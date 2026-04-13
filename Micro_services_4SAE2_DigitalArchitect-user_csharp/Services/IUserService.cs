using microservice_user.Models.DTOs;

namespace microservice_user.Services;

public interface IUserService
{
    Task<UserResponseDto?> CreateUserAsync(UserCreateDto dto);
    Task<UserResponseDto?> GetUserByIdAsync(long id);
    Task<UserResponseDto?> GetUserByEmailAsync(string email);
    Task<UserResponseDto?> UpdateUserAsync(long id, UserUpdateDto dto);
    Task<bool> DeleteUserAsync(long id);
    Task<IEnumerable<UserResponseDto>> GetUsersByRoleAsync(string role);
}
