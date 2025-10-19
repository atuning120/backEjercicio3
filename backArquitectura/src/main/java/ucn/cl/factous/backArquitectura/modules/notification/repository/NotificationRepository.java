package ucn.cl.factous.backArquitectura.modules.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ucn.cl.factous.backArquitectura.modules.notification.entity.Notification;

import java.util.List;

@Repository
public interface NotificationRepository  extends JpaRepository<Notification, Long> {

    List<Notification> findByUserId(Long userId);
    List<Notification> findByUserIdIsNull();
    List<Notification> findByUserIdAndIsReadFalseOrderByTimestampDesc(Long userId);

}
