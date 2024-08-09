package beyondProjectForOrdersystem.ordering.repository;

import beyondProjectForOrdersystem.member.domain.Member;
import beyondProjectForOrdersystem.ordering.domain.Ordering;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderingRepository extends JpaRepository<Ordering,Long> {
    Page<Ordering> findAllByMember(Member member, Pageable pageable);
}
