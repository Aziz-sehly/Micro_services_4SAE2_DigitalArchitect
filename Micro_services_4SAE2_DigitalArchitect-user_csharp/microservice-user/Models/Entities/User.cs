using microservice_user.Models.Enums;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Security.Claims;

namespace microservice_user.Models.Entities;

[Table("users")]
public class User
{
    [Key]
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public long Id { get; set; }

    [Required]
    [EmailAddress]
    [Column(TypeName = "varchar(255)")]
    public string Email { get; set; } = string.Empty;

    [Required]
    [Column(TypeName = "text")]
    public string Password { get; set; } = string.Empty;

    [Required]
    [Column(TypeName = "varchar(100)")]
    public string FirstName { get; set; } = string.Empty;

    [Required]
    [Column(TypeName = "varchar(100)")]
    public string LastName { get; set; } = string.Empty;

    [Required]
    [Column(TypeName = "varchar(20)")]
    public Role Role { get; set; }

    [Column(TypeName = "varchar(500)")]
    public string? ProfilePicture { get; set; }

    [Column(TypeName = "text")]
    public string? Bio { get; set; }

    [Column(TypeName = "varchar(20)")]
    public string? PhoneNumber { get; set; }

    [Column(TypeName = "text")]
    public string? Skills { get; set; }

    [Column(TypeName = "varchar(500)")]
    public string? PortfolioUrl { get; set; }

    [Column(TypeName = "varchar(200)")]
    public string? CompanyName { get; set; }

    [Required]
    public bool IsVerified { get; set; } = false;

    [Required]
    public bool IsActive { get; set; } = true;

    [Column(TypeName = "varchar(500)")]
    public string? VerificationToken { get; set; }

    public DateTime? VerificationTokenExpiry { get; set; }

    [Required]
    public DateTime CreatedAt { get; set; }

    public DateTime? UpdatedAt { get; set; }

    [Column(TypeName = "varchar(100)")]
    public string? KeycloakId { get; set; }

    public string FullName => $"{FirstName} {LastName}".Trim();

    public IEnumerable<Claim> GetClaims()
    {
        return new List<Claim>
        {
            new Claim(ClaimTypes.NameIdentifier, Id.ToString()),
            new Claim(ClaimTypes.Email, Email),
            new Claim(ClaimTypes.Name, FullName),
            new Claim(ClaimTypes.Role, Role.ToString()),
            new Claim("KeycloakId", KeycloakId ?? string.Empty)
        };
    }
}