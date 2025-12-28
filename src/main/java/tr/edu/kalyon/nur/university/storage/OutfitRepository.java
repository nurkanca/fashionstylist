package tr.edu.kalyon.nur.university.storage;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tr.edu.kalyon.nur.university.storage.entity.Outfit;

import java.util.List;
import java.util.Optional;

public interface OutfitRepository extends JpaRepository<Outfit, Long> {
    List<Outfit> findByUserIdOrderByScoreDesc(Long userId);

    @Query("""
  select distinct o
  from Outfit o
  left join fetch o.clothes c
  where o.user.id = :userId
    and o.score >= 0.90
  order by o.score desc
  """)
    List<Outfit> findTopCandidates(@Param("userId") Long userId);

    @Query("""
    select distinct o
    from Outfit o
    left join fetch o.clothes c
    where o.user.id = :userId
    and o.id in :ids
  """)
    List<Outfit> findByUserAndIds(@Param("userId") Long userId, @Param("ids") List<Long> ids);

    @Query("""
      select distinct o
      from Outfit o
      left join fetch o.clothes c
      where o.id = :outfitId
    """)
    Optional<Outfit> findByIdWithClothes(@Param("outfitId") Long outfitId);

    @Query("""
    select distinct o
    from Outfit o
    join o.clothes c
    join c.seasons s
    where o.user.id = :userId
      and o.score >= 0.90
      and s in :seasons
    order by o.score desc
""")
    List<Outfit> findHighScoreOutfitsBySeasons(
            @Param("userId") Long userId,
            @Param("seasons") List<String> seasons
    );



    @Query("""
    select distinct o
    from Outfit o
    join fetch o.clothes c
    where o.user.id = :userId
      and o.score >= 0.90
      and not exists (
          select 1
          from Clothe cx
          join o.clothes c2
          where c2 = cx
            and :season not in (select s from cx.seasons s)
      )
    order by o.score desc
""")
    List<Outfit> findHighScoreOutfitsWhereAllClothesInSeason(
            @Param("userId") Long userId,
            @Param("season") String season
    );

    @Query("""
    select distinct o
    from Outfit o
    join fetch o.clothes c
    where o.user.id = :userId
      and o.score >= 0.90
      and not exists (
          select 1
          from Clothe cx
          join o.clothes c2
          where c2 = cx
            and not exists (
                select 1
                from cx.seasons s
                where s in :seasons
            )
      )
    order by o.score desc
""")
    List<Outfit> findHighScoreOutfitsWhereAllClothesMatchAnySeasonInList(
            @Param("userId") Long userId,
            @Param("seasons") List<String> seasons
    );



    @Query("""
        select distinct o
        from Outfit o
        left join fetch o.clothes c
        where o.id = :outfitId
          and o.user.id = :userId
    """)
    Optional<Outfit> findByIdAndUserIdFetchClothes(
            @Param("outfitId") Long outfitId,
            @Param("userId") Long userId
    );


    Optional<Outfit> findByIdAndUserId(Long outfitId, Long userId);
}
