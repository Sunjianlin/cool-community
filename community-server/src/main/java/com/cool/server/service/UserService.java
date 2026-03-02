package com.cool.server.service;

import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.dto.UserLoginDTO;
import com.cool.pojo.dto.UserRegisterDTO;
import com.cool.pojo.vo.PageVO;
import com.cool.pojo.vo.UserLoginVO;
import com.cool.pojo.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

public interface UserService {
    
    UserLoginVO login(UserLoginDTO dto);

    void register(UserRegisterDTO dto);

    void logout();

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

    PageVO<UserVO> getFollowingList(Long userId, PageQueryDTO dto);

    PageVO<UserVO> getFollowerList(Long userId, PageQueryDTO dto);

    boolean isFollowing(Long userId, Long targetId);
    
    void kickUser(Long id);
    
    Map<String, Object> getOnlineUserStats();
}
