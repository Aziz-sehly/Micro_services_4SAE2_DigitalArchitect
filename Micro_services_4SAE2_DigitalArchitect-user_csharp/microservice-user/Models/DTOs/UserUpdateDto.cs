public class UserUpdateDto
{
    public string? FirstName { get; set; }
    public string? LastName { get; set; }
    public string? ProfilePicture { get; set; }
    public string? Bio { get; set; }
    public string? PhoneNumber { get; set; }
    public string? Skills { get; set; }
    public string? PortfolioUrl { get; set; }
    public string? CompanyName { get; set; }
    public bool? IsActive { get; set; }
    public bool? IsVerified { get; set; }  // ← ADD THIS (if not exists)
}