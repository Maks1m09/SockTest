package by.testSocks.serviceTest;

import by.testSocks.entity.Sock;
import by.testSocks.repository.SockRepository;
import by.testSocks.service.SockService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SockServiceTest {


    @InjectMocks
    private SockService sockService;

    @Mock
    private SockRepository sockRepository;

    @Test
    public void testAddIncome_SockExists_UpdatesQuantity() {
        Sock existingSock = new Sock(1L, "blue", 80.0, 5);
        Sock incomingSock = new Sock(1L, "blue", 80.0, 3);

        when(sockRepository.findById(1L)).thenReturn(Optional.of(existingSock));
        when(sockRepository.save(any(Sock.class))).thenReturn(existingSock);

        Sock updatedSock = sockService.addIncome(incomingSock);

        assertThat(updatedSock.getQuantity()).isEqualTo(8); // 5 + 3
        verify(sockRepository, times(1)).findById(1L);
        verify(sockRepository, times(1)).save(existingSock);
    }

    @Test
    public void testAddIncome_SockDoesNotExist_CreatesNewSock() {
        Sock incomingSock = new Sock(1L, "blue", 80.0, 3);

        when(sockRepository.findById(1L)).thenReturn(Optional.empty());
        when(sockRepository.save(any(Sock.class))).thenReturn(incomingSock);

        Sock createdSock = sockService.addIncome(incomingSock);

        assertThat(createdSock.getQuantity()).isEqualTo(3);
        verify(sockRepository, times(1)).findById(1L);
        verify(sockRepository, times(1)).save(incomingSock);
    }

    @Test
    public void testAddOutcome_SufficientQuantity_UpdatesQuantity() {
        Sock existingSock = new Sock(1L, "blue", 80.0, 5);
        Sock outcomeSock = new Sock(1L, "blue", 80.0, 3);

        when(sockRepository.findById(1L)).thenReturn(Optional.of(existingSock));
        when(sockRepository.save(any(Sock.class))).thenReturn(existingSock);

        Sock updatedSock = sockService.addOutcome(outcomeSock);

        assertThat(updatedSock.getQuantity()).isEqualTo(2); // 5 - 3
        verify(sockRepository, times(1)).findById(1L);
        verify(sockRepository, times(1)).save(existingSock);
    }

    @Test
    public void testAddOutcome_InsufficientQuantity_ThrowsException() {
        Sock existingSock = new Sock(1L, "blue", 80.0, 2);
        Sock outcomeSock = new Sock(1L, "blue", 80.0, 3);

        when(sockRepository.findById(1L)).thenReturn(Optional.of(existingSock));

        Exception thrown = org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            sockService.addOutcome(outcomeSock);
        });

        assertThat(thrown.getMessage()).contains("Not enough socks in stock");
    }

    @Test
    public void testUpdateSock_SockExists_UpdatesSock() {
        Sock existingSock = new Sock(1L, "blue", 80.0, 5);
        Sock newSockData = new Sock(null, "green", 75.0, 10);

        when(sockRepository.findById(1L)).thenReturn(Optional.of(existingSock));
        when(sockRepository.save(any(Sock.class))).thenReturn(newSockData);

        Sock updatedSock = sockService.updateSock(1L, newSockData);

        assertThat(updatedSock.getColor()).isEqualTo("green");
        assertThat(updatedSock.getCottonPercentage()).isEqualTo(75.0);
        assertThat(updatedSock.getQuantity()).isEqualTo(10);
        verify(sockRepository, times(1)).findById(1L);
        verify(sockRepository, times(1)).save(any(Sock.class));
    }

    @Test
    public void testUpdateSock_SockDoesNotExist_ThrowsException() {
        Sock newSockData = new Sock(null, "green", 75.0, 10);

        when(sockRepository.findById(1L)).thenReturn(Optional.empty());

        Exception thrown = org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
            sockService.updateSock(1L, newSockData);
        });

        assertThat(thrown.getMessage()).contains("Sock not found");
    }
}
