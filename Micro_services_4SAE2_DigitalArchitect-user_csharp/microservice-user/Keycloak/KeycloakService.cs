using System.Net.Http.Headers;
using System.Text;
using System.Text.Json;

namespace microservice_user.Keycloak;

public class KeycloakService : IKeycloakService
{
    private readonly IConfiguration _configuration;
    private readonly HttpClient _httpClient;
    private readonly ILogger<KeycloakService> _logger;

    public KeycloakService(IConfiguration configuration, ILogger<KeycloakService> logger)
    {
        _configuration = configuration;
        _logger = logger;
        _httpClient = new HttpClient();
    }

    public async Task<string?> GetAdminTokenAsync()
    {
        var keycloakUrl = _configuration["Keycloak:Url"];
        var realm = _configuration["Keycloak:Realm"];
        var clientId = _configuration["Keycloak:AdminClientId"];
        var clientSecret = _configuration["Keycloak:AdminClientSecret"];

        var tokenUrl = $"{keycloakUrl}/realms/{realm}/protocol/openid-connect/token";

        var content = new FormUrlEncodedContent(new[]
        {
            new KeyValuePair<string, string>("grant_type", "client_credentials"),
            new KeyValuePair<string, string>("client_id", clientId),
            new KeyValuePair<string, string>("client_secret", clientSecret)
        });

        try
        {
            var response = await _httpClient.PostAsync(tokenUrl, content);
            var responseString = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
            {
                _logger.LogError("Failed to get admin token: {Response}", responseString);
                return null;
            }

            using var doc = JsonDocument.Parse(responseString);
            return doc.RootElement.GetProperty("access_token").GetString();
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Exception getting admin token");
            return null;
        }
    }

    public async Task<string?> CreateUserAsync(string email, string firstName, string lastName, string password, string role)
    {
        var token = await GetAdminTokenAsync();
        if (token == null) return null;

        var keycloakUrl = _configuration["Keycloak:Url"];
        var realm = _configuration["Keycloak:Realm"];
        var createUserUrl = $"{keycloakUrl}/admin/realms/{realm}/users";

        var userPayload = new
        {
            username = email,
            email = email,
            firstName = firstName,
            lastName = lastName,
            enabled = true,
            emailVerified = false,
            credentials = new[]
            {
                new
                {
                    type = "password",
                    value = password,
                    temporary = false
                }
            },
            attributes = new Dictionary<string, string[]>
            {
                { "origin", new[] { "microservice-user" } }
            }
        };

        var json = JsonSerializer.Serialize(userPayload);
        var content = new StringContent(json, Encoding.UTF8, "application/json");

        _httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);

        try
        {
            var response = await _httpClient.PostAsync(createUserUrl, content);

            if (!response.IsSuccessStatusCode)
            {
                var error = await response.Content.ReadAsStringAsync();
                _logger.LogError("Failed to create Keycloak user: {Error}", error);
                return null;
            }

            var location = response.Headers.Location?.ToString();
            if (location != null)
            {
                var userId = location.Split('/').Last();
                await AssignRoleToUserAsync(userId, role, token);
                return userId;
            }

            return null;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Exception creating Keycloak user");
            return null;
        }
    }

    private async Task<bool> AssignRoleToUserAsync(string userId, string roleName, string adminToken)
    {
        var keycloakUrl = _configuration["Keycloak:Url"];
        var realm = _configuration["Keycloak:Realm"];

        var roleUrl = $"{keycloakUrl}/admin/realms/{realm}/roles/{roleName}";
        _httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", adminToken);

        var roleResponse = await _httpClient.GetAsync(roleUrl);
        if (!roleResponse.IsSuccessStatusCode)
        {
            await CreateRealmRoleAsync(roleName, adminToken);
            roleResponse = await _httpClient.GetAsync(roleUrl);
        }

        if (!roleResponse.IsSuccessStatusCode) return false;

        var roleJson = await roleResponse.Content.ReadAsStringAsync();
        using var roleDoc = JsonDocument.Parse(roleJson);
        var roleId = roleDoc.RootElement.GetProperty("id").GetString();

        var assignUrl = $"{keycloakUrl}/admin/realms/{realm}/users/{userId}/role-mappings/realm";
        var assignPayload = new[] { new { id = roleId, name = roleName } };
        var assignContent = new StringContent(JsonSerializer.Serialize(assignPayload), Encoding.UTF8, "application/json");

        var assignResponse = await _httpClient.PostAsync(assignUrl, assignContent);
        return assignResponse.IsSuccessStatusCode;
    }

    private async Task CreateRealmRoleAsync(string roleName, string adminToken)
    {
        var keycloakUrl = _configuration["Keycloak:Url"];
        var realm = _configuration["Keycloak:Realm"];
        var createRoleUrl = $"{keycloakUrl}/admin/realms/{realm}/roles";

        var rolePayload = new { name = roleName };
        var content = new StringContent(JsonSerializer.Serialize(rolePayload), Encoding.UTF8, "application/json");

        _httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", adminToken);
        await _httpClient.PostAsync(createRoleUrl, content);
    }

    public async Task<bool> DeleteUserAsync(string keycloakUserId)
    {
        var token = await GetAdminTokenAsync();
        if (token == null) return false;

        var keycloakUrl = _configuration["Keycloak:Url"];
        var realm = _configuration["Keycloak:Realm"];
        var deleteUrl = $"{keycloakUrl}/admin/realms/{realm}/users/{keycloakUserId}";

        _httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);

        var response = await _httpClient.DeleteAsync(deleteUrl);
        return response.IsSuccessStatusCode;
    }

    public async Task<bool> UpdateUserAsync(string keycloakUserId, string? firstName, string? lastName, string? email)
    {
        var token = await GetAdminTokenAsync();
        if (token == null) return false;

        var keycloakUrl = _configuration["Keycloak:Url"];
        var realm = _configuration["Keycloak:Realm"];
        var updateUrl = $"{keycloakUrl}/admin/realms/{realm}/users/{keycloakUserId}";

        var updatePayload = new Dictionary<string, object>();
        if (firstName != null) updatePayload["firstName"] = firstName;
        if (lastName != null) updatePayload["lastName"] = lastName;
        if (email != null) updatePayload["email"] = email;

        var content = new StringContent(JsonSerializer.Serialize(updatePayload), Encoding.UTF8, "application/json");
        _httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);

        var response = await _httpClient.PutAsync(updateUrl, content);
        return response.IsSuccessStatusCode;
    }

    public async Task<bool> UpdateEmailVerifiedAsync(string keycloakUserId, bool emailVerified)
    {
        var token = await GetAdminTokenAsync();
        if (token == null) return false;

        var keycloakUrl = _configuration["Keycloak:Url"];
        var realm = _configuration["Keycloak:Realm"];
        var updateUrl = $"{keycloakUrl}/admin/realms/{realm}/users/{keycloakUserId}";

        var updatePayload = new
        {
            emailVerified = emailVerified
        };

        var content = new StringContent(
            JsonSerializer.Serialize(updatePayload),
            Encoding.UTF8,
            "application/json"
        );

        _httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);

        try
        {
            var response = await _httpClient.PutAsync(updateUrl, content);

            if (!response.IsSuccessStatusCode)
            {
                var error = await response.Content.ReadAsStringAsync();
                _logger.LogError("Failed to update emailVerified in Keycloak: {Error}", error);
                return false;
            }

            return true;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Exception updating emailVerified in Keycloak");
            return false;
        }
    }
}