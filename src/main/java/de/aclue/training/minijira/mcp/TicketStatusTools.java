package de.aclue.training.minijira.mcp;

import de.aclue.training.minijira.domain.Issue;
import de.aclue.training.minijira.domain.Status;
import de.aclue.training.minijira.store.IssueStore;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

@Component
public class TicketStatusTools {

    private static final Logger log = LoggerFactory.getLogger(TicketStatusTools.class);

    private final IssueStore store;

    public TicketStatusTools(IssueStore store) {
        this.store = store;
    }

    @McpTool(
            name = "update_ticket_status",
            description = "Changes the status of a single ticket, e.g. from BLOCKED to DONE. Modifies data: the user is asked to confirm before anything changes. Only use it when the user explicitly asks for a status change.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = false, destructiveHint = false))
    public String updateTicketStatus(
            @McpToolParam(description = "Ticket key, e.g. SHOP-103") String key,
            @McpToolParam(description = "The new status of the ticket") Status status,
            McpSyncServerExchange exchange) {

        log.info("Tool call: update_ticket_status(key={}, status={})", key, status);

        Issue issue = store.findByKey(key)
                .orElseThrow(() -> new ToolCallException(
                        "Ticket %s not found. Tip: use search_issues.".formatted(key)));

        if (issue.status() == status) {
            return "%s is already %s. Nothing changed.".formatted(key, status);
        }

        // No confirmation, no change: a client that can't ask the user doesn't get to change data.
        McpSchema.ClientCapabilities capabilities = exchange.getClientCapabilities();
        if (capabilities == null || capabilities.elicitation() == null) {
            throw new ToolCallException(
                    "This client can't ask the user for confirmation, so the status of %s was not changed."
                            .formatted(key));
        }

        // Ask the human directly, through the host. The model never sees this form.
        // No fields: the user answers with Accept or Decline.
        McpSchema.ElicitResult answer = exchange.createElicitation(McpSchema.ElicitRequest.builder()
                .message("Change the status of %s (%s) from %s to %s?"
                        .formatted(key, issue.summary(), issue.status(), status))
                .requestedSchema(Map.of("type", "object", "properties", Map.of()))
                .build());

        if (answer.action() != McpSchema.ElicitResult.Action.ACCEPT) {
            log.info("Status change of {} not confirmed by the user ({})", key, answer.action());
            throw new ToolCallException(
                    "The user did not confirm the status change of %s. Nothing changed. Don't try again; ask the user what to do instead."
                            .formatted(key));
        }

        store.updateStatus(key, status, LocalDate.now());
        log.info("Status of {} changed from {} to {}", key, issue.status(), status);
        return "Status of %s changed from %s to %s.".formatted(key, issue.status(), status);
    }
}
