using microservice_user.Models.Enums;
using System.ComponentModel.DataAnnotations;

namespace microservice_user.Models.DTOs;

public class UserCreateDto
{
    [Required]
    [EmailAddress]
    public string Email { get; set; } = string.Empty;

    [Required]
    [MinLength(6)]
    public string Password { get; set; } = string.Empty;

    [Required]
    public string FirstName { get; set; } = string.Empty;

    [Required]
    public string LastName { get; set; } = string.Empty;

    [Required]
    public Role Role { get; set; }

    public string? ProfilePicture { get; set; }
    public string? Bio { get; set; }
    public string? PhoneNumber { get; set; }
    public string? Skills { get; set; }
    public string? PortfolioUrl { get; set; }
    public string? CompanyName { get; set; }
}
