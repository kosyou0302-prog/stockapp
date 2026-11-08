/*
 * 【役割】家族（グループ）およびユーザー情報に対するデータベース操作を担当します。
 * ※初期開発フェーズではログイン機能がないため、中身は空（もしくは後回し）でOKです。
 *
 * TODO: 後日、ログイン機能やユーザー登録機能を実装する際に追記する
 *
 * 【今回の追加】
 * 　ログイン機能に先立ち、買い物リストの同時操作に対応するため、
 * 　家族データをロック付きで取得する処理を追加します。
 */
package com.example.stockapp.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.stockapp.entities.Family;

import jakarta.persistence.LockModeType;

@Repository
public interface FamilyRepository extends JpaRepository<Family, Long> {
    /*
     * 【処理内容】家族データをロック付きで取得する
     *
     * 同じ家族の買い物リストへの追加・購入完了処理を
     * 順番に行うために使用します。
     *
     * ・先の処理がロックを取得している間、
     *   後から同じ家族のロックを取得する処理は待機します。
     * ・ロックはトランザクション終了時に解放されます。
     * ・家族名などのデータを変更する処理ではありません。
     *
     * ※Serviceの@Transactionalが有効な処理から呼び出します。
     * ※関連する更新処理も、同じ順序でロックを取得する必要があります。
     *
     * id：取得する家族のID（現在はダミーの1L）
     * 戻り値：該当する家族。存在しない場合は空のOptional。
     */

    // 対象の家族データに更新用のロックを取得する
    @Lock(LockModeType.PESSIMISTIC_WRITE)

    // FamilyはEntity名。指定されたIDの家族を取得する
    @Query("select f from Family f where f.id = :id")
    Optional<Family> findByIdForUpdate(@Param("id") Long id);

}
