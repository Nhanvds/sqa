package com.doan.backend.repositories;

import com.doan.backend.entity.ChatRoomUser;
import com.doan.backend.entity.User;
import com.doan.backend.enums.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUser, String> {
    List<ChatRoomUser> findByChatRoomId(String chatRoomId);

    List<ChatRoomUser> findByUserId(String userId);

    @Query("SELECT c.user FROM ChatRoomUser c WHERE c.chatRoom.id = :chatRoomId")
    List<User> findUserByChatRoomId(@Param("chatRoomId") String chatRoomId);

    Optional<ChatRoomUser> findChatRoomUserByChatRoomIdAndUserId(String chatRoomId, String userId);

    Boolean existsByChatRoomIdAndUserId(String chatRoomId, String userId);

    boolean existsByChatRoomIdAndUser_RolesIn(String chatRoomId, Set<RoleEnum> roles);
}
