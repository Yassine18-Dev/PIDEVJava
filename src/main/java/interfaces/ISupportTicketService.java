package interfaces;

import entities.SupportTicket;
import java.util.List;

public interface ISupportTicketService {
    void add(SupportTicket ticket);
    void update(SupportTicket ticket);
    void delete(int id);
    List<SupportTicket> getAll();
    SupportTicket getById(int id);
}