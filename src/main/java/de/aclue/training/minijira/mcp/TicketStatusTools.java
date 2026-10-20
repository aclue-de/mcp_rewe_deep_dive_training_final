package de.aclue.training.minijira.mcp;

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

    // ============================================================
    // Lab 2 · Part D: implement update_ticket_status here.
    // Task and syntax: see project_docs/LABS_PLAYBOOK.md, Lab 2 Part D.
    // Optional self-check: TicketStatusToolsTest.
    // ============================================================
    @McpTool(
            name = "update_ticket_status",
            description = "TODO: describe what this tool changes, and that the model should only use it when the user explicitly asks for a status change.")
    // TODO: tell the host that this tool changes data (annotations, see the syntax reference)
    public String updateTicketStatus(
            @McpToolParam(description = "TODO: describe the key parameter, with an example") String key,
            @McpToolParam(description = "TODO: describe the status parameter") Status status,
            McpSyncServerExchange exchange) {

        log.info("Tool call: update_ticket_status(key={}, status={})", key, status);

        // TODO: unknown ticket? Throw a ToolCallException (look at get_issue in IssueTools)
        // TODO: client can't show a form (exchange.getClientCapabilities().elicitation() == null)?
        //       Throw a ToolCallException: no confirmation, no change
        // TODO: ask the user with exchange.createElicitation(...) (see "Asking the user" in the playbook)
        // TODO: only if the user accepted (ACCEPT): store.updateStatus(key, status, LocalDate.now())
        //       otherwise throw a ToolCallException that tells the model not to try again
        return "TODO";
    }
}
