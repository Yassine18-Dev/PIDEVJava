package interfaces;

import entities.SupportMessage;
import java.util.List;

public interface ISupportMessageService {
    void add(SupportMessage message);
    void delete(int id);
    List<SupportMessage> getByTicket(int ticketId);
}