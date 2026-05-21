package com.esun.financialsystem.business.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.esun.financialsystem.common.exception.BadRequestException;
import com.esun.financialsystem.common.exception.ResourceNotFoundException;
import com.esun.financialsystem.data.repository.UserRepository;
import com.esun.financialsystem.presentation.request.GetUserRequest;
import com.esun.financialsystem.presentation.request.PostUserRequest;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getUserRejectsInvalidSortDirection() {
        GetUserRequest request = new GetUserRequest();
        request.setSortDirection("SIDEWAYS");

        assertThatThrownBy(() -> userService.getUser(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("sortDirection must be ASC or DESC");
        verifyNoInteractions(userRepository);
    }

    @Test
    void postUserTrimsFieldsAndDelegatesToRepository() {
        PostUserRequest request = new PostUserRequest(
                " A1236456789 ",
                " 王小明 ",
                " test@email.com ",
                " 1111999666 ");

        when(userRepository.postUser(
                "A1236456789",
                "王小明",
                "test@email.com",
                "1111999666"))
                .thenReturn("A1236456789");

        String userId = userService.postUser(request);

        assertThat(userId).isEqualTo("A1236456789");
        verify(userRepository).postUser(
                "A1236456789",
                "王小明",
                "test@email.com",
                "1111999666");
    }

    @Test
    void getUserByIdRejectsBlankUserId() {
        assertThatThrownBy(() -> userService.getUserById(" "))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("userId must not be blank");
        verifyNoInteractions(userRepository);
    }

    @Test
    void getUserByIdThrowsWhenMissing() {
        when(userRepository.getUserById("Z9999999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById("Z9999999999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User Z9999999999 does not exist");
    }
}
