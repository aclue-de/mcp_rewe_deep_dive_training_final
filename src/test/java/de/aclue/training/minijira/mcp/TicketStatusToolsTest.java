package de.aclue.training.minijira.mcp;

import de.aclue.training.minijira.domain.Issue;
import de.aclue.training.minijira.domain.Priority;
import de.aclue.training.minijira.domain.Status;
import de.aclue.training.minijira.store.IssueStore;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.ClientCapabilities;
import io.modelcontextprotocol.spec.McpSchema.ElicitResult;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Disabled("Lab 2 · Part D: remove @Disabled once update_ticket_status is implemented")
class TicketStatusToolsTest {

    @Mock
    private IssueStore store;

    @Mock
    private McpSyncServerExchange exchange;

    @InjectMocks
    private TicketStatusTools ticketStatusTools;

    @Test
    void unknownTicketIsAToolError() {
        when(store.findByKey("SHOP-999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketStatusTools.updateTicketStatus("SHOP-999", Status.DONE, exchange))
                .isInstanceOf(ToolCallException.class)
                .hasMessageContaining("SHOP-999");
        verify(store, never()).updateStatus(any(), any(), any());
    }

    @Test
    void refusesWhenTheClientCannotAskTheUser() {
        when(store.findByKey("SHOP-103")).thenReturn(Optional.of(blockedIssue()));
        when(exchange.getClientCapabilities()).thenReturn(ClientCapabilities.builder().build());

        assertThatThrownBy(() -> ticketStatusTools.updateTicketStatus("SHOP-103", Status.DONE, exchange))
                .isInstanceOf(ToolCallException.class);
        verify(store, never()).updateStatus(any(), any(), any());
    }

    @Test
    void changesTheStatusAfterTheUserConfirmed() {
        when(store.findByKey("SHOP-103")).thenReturn(Optional.of(blockedIssue()));
        when(exchange.getClientCapabilities()).thenReturn(ClientCapabilities.builder().elicitation().build());
        when(exchange.createElicitation(any()))
                .thenReturn(new ElicitResult(ElicitResult.Action.ACCEPT, Map.of()));

        String result = ticketStatusTools.updateTicketStatus("SHOP-103", Status.DONE, exchange);

        assertThat(result).contains("SHOP-103").contains("DONE");
        verify(store).updateStatus(eq("SHOP-103"), eq(Status.DONE), any());
    }

    @Test
    void keepsTheStatusWhenTheUserDeclined() {
        when(store.findByKey("SHOP-103")).thenReturn(Optional.of(blockedIssue()));
        when(exchange.getClientCapabilities()).thenReturn(ClientCapabilities.builder().elicitation().build());
        when(exchange.createElicitation(any())).thenReturn(new ElicitResult(ElicitResult.Action.DECLINE, null));

        assertThatThrownBy(() -> ticketStatusTools.updateTicketStatus("SHOP-103", Status.DONE, exchange))
                .isInstanceOf(ToolCallException.class);
        verify(store, never()).updateStatus(any(), any(), any());
    }

    @Test
    void keepsTheStatusWhenTheUserClosedTheForm() {
        when(store.findByKey("SHOP-103")).thenReturn(Optional.of(blockedIssue()));
        when(exchange.getClientCapabilities()).thenReturn(ClientCapabilities.builder().elicitation().build());
        when(exchange.createElicitation(any())).thenReturn(new ElicitResult(ElicitResult.Action.CANCEL, null));

        assertThatThrownBy(() -> ticketStatusTools.updateTicketStatus("SHOP-103", Status.DONE, exchange))
                .isInstanceOf(ToolCallException.class);
        verify(store, never()).updateStatus(any(), any(), any());
    }

    private static Issue blockedIssue() {
        return new Issue("SHOP-103", "Checkout fails", "Description", Status.BLOCKED, Priority.HIGH, "lena",
                "Sprint 42", List.of(), List.of(), LocalDate.of(2026, 9, 1));
    }
}
