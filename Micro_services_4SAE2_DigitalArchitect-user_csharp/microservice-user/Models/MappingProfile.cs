using AutoMapper;
using microservice_user.Models.DTOs;
using microservice_user.Models.Entities;

namespace microservice_user.Models;

public class MappingProfile : Profile
{
    public MappingProfile()
    {
        CreateMap<User, UserResponseDto>();
    }
}