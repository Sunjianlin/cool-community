package com.cool.server.service;

import com.cool.pojo.dto.UserLoginDTO;
import com.cool.pojo.dto.UserRegisterDTO;
import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.vo.UserLoginVO;
import com.cool.pojo.vo.UserVO;
import com.cool.pojo.vo.PageVO;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    
    void register(UserRegisterDTO dto);

    UserLoginVO login(UserLoginDTO dto);

    UserVO getUserInfo();

    UserVO getUserInfoById(Long id);

    void updateUserInfo(UserVO vo);

    String uploadAvatar(MultipartFile file);

    void followUser(Long id);

    void unfollowUser(Long id);

    PageVO<UserVO> getUserList(PageQueryDTO dto);

    void banUser(Long id);

    void unbanUser(Long id);

    void deleteUser(Long id);
}
