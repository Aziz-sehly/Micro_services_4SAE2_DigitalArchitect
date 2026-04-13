namespace microservice_user.Keycloak;

public interface IKeycloakService
{
    Task<string?> CreateUserAsync(string email, string firstName, string lastName, string password, string role);
    Task<bool> DeleteUserAsync(string keycloakUserId);
    Task<bool> UpdateUserAsync(string keycloakUserId, string? firstName, string? lastName, string? email);
    Task<string?> GetAdminTokenAsync();
    Task<bool> UpdateEmailVerifiedAsync(string keycloakUserId, bool emailVerified);  // ✅ NEW
}