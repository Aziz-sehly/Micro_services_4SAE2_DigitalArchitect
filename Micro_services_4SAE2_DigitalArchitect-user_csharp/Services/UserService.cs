using AutoMapper;
using microservice_user.Data;
using microservice_user.Keycloak;
using microservice_user.Models.DTOs;
using microservice_user.Models.Entities;
using Microsoft.EntityFrameworkCore;

namespace microservice_user.Services;

public class UserService : IUserService
{
    private readonly UserDbContext _context;
    private readonly IKeycloakService _keycloakService;
    private readonly IMapper _mapper;
    private readonly ILogger<UserService> _logger;

    public UserService(UserDbContext context, IKeycloakService keycloakService, IMapper mapper, ILogger<UserService> logger)
    {
        _context = context;
        _keycloakService = keycloakService;
        _mapper = mapper;
        _logger = logger;
    }

    public async Task<UserResponseDto?> CreateUserAsync(UserCreateDto dto)
    {
        if (await _context.Users.AnyAsync(u => u.Email == dto.Email))
        {
            _logger.LogWarning("User with email {Email} already exists", dto.Email);
            return null;
        }

        var keycloakId = await _keycloakService.CreateUserAsync(
            dto.Email, 
            dto.FirstName, 
            dto.LastName, 
            dto.Password,
            dto.Role.ToString()
        );

        if (keycloakId == null)
        {
            _logger.LogError("Failed to create user in Keycloak");
            return null;
        }

        try
        {
            var hashedPassword = BCrypt.Net.BCrypt.HashPassword(dto.Password);

            var user = new User
            {
                Email = dto.Email,
                Password = hashedPassword,
                FirstName = dto.FirstName,
                LastName = dto.LastName,
                Role = dto.Role,
                ProfilePicture = dto.ProfilePicture,
                Bio = dto.Bio,
                PhoneNumber = dto.PhoneNumber,
                Skills = dto.Skills,
                PortfolioUrl = dto.PortfolioUrl,
                CompanyName = dto.CompanyName,
                KeycloakId = keycloakId,
                CreatedAt = DateTime.UtcNow
            };

            _context.Users.Add(user);
            await _context.SaveChangesAsync();

            _logger.LogInformation("User {Email} created with ID {Id}", dto.Email, user.Id);
            return _mapper.Map<UserResponseDto>(user);
        }
        catch (Exception ex)
        {
            await _keycloakService.DeleteUserAsync(keycloakId);
            _logger.LogError(ex, "Failed to save user to database");
            throw;
        }
    }
    public async Task<UserResponseDto?> GetUserByIdAsync(long id)
    {
        var user = await _context.Users.FindAsync(id);
        return user == null ? null : _mapper.Map<UserResponseDto>(user);
    }

    public async Task<UserResponseDto?> GetUserByEmailAsync(string email)
    {
        var user = await _context.Users.FirstOrDefaultAsync(u => u.Email == email);
        return user == null ? null : _mapper.Map<UserResponseDto>(user);
    }

    public async Task<UserResponseDto?> UpdateUserAsync(long id, UserUpdateDto dto)
    {
        var user = await _context.Users.FindAsync(id);
        if (user == null) return null;

        if (dto.FirstName != null) user.FirstName = dto.FirstName;
        if (dto.LastName != null) user.LastName = dto.LastName;
        if (dto.ProfilePicture != null) user.ProfilePicture = dto.ProfilePicture;
        if (dto.Bio != null) user.Bio = dto.Bio;
        if (dto.PhoneNumber != null) user.PhoneNumber = dto.PhoneNumber;
        if (dto.Skills != null) user.Skills = dto.Skills;
        if (dto.PortfolioUrl != null) user.PortfolioUrl = dto.PortfolioUrl;
        if (dto.CompanyName != null) user.CompanyName = dto.CompanyName;
        if (dto.IsActive.HasValue) user.IsActive = dto.IsActive.Value;

        user.UpdatedAt = DateTime.UtcNow;

        if (user.KeycloakId != null)
        {
            await _keycloakService.UpdateUserAsync(
                user.KeycloakId,
                dto.FirstName,
                dto.LastName,
                null
            );
        }

        await _context.SaveChangesAsync();
        return _mapper.Map<UserResponseDto>(user);
    }

    public async Task<bool> DeleteUserAsync(long id)
    {
        var user = await _context.Users.FindAsync(id);
        if (user == null) return false;

        if (user.KeycloakId != null)
        {
            await _keycloakService.DeleteUserAsync(user.KeycloakId);
        }

        _context.Users.Remove(user);
        await _context.SaveChangesAsync();
        return true;
    }

    public async Task<IEnumerable<UserResponseDto>> GetUsersByRoleAsync(string role)
    {
        var users = await _context.Users
            .Where(u => u.Role.ToString() == role && u.IsActive)
            .ToListAsync();
        return _mapper.Map<IEnumerable<UserResponseDto>>(users);
    }
}
