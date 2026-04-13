using microservice_user.Models.Enums;

namespace microservice_user.Models.DTOs;

public class UserResponseDto
{
    public long Id { get; set; }
    public string Email { get; set; } = string.Empty;
    public string FirstName { get; set; } = string.Empty;
    public string LastName { get; set; } = string.Empty;
    public string FullName => $"{FirstName} {LastName}".Trim();
    public Role Role { get; set; }
    public string? ProfilePicture { get; set; }
    public string? Bio { get; set; }
    public string? PhoneNumber { get; set; }
    public string? Skills { get; set; }
    public string? PortfolioUrl { get; set; }
    public string? CompanyName { get; set; }
    public bool IsVerified { get; set; }
    public bool IsActive { get; set; }
    public DateTime CreatedAt { get; set; }
    public DateTime? UpdatedAt { get; set; }
    public string? KeycloakId { get; set; }
}