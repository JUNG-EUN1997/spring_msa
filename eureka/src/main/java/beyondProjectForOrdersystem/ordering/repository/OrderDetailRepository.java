package beyondProjectForOrdersystem.ordering.repository;

import beyondProjectForOrdersystem.ordering.domain.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail,Long> {

}
