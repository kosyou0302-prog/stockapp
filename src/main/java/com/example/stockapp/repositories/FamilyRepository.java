/*
 * 【役割】家族テーブル（families）に対するデータベース操作を担当します。
 *
 * ログイン機能実装後は家族・ユーザー情報の管理全般を担います。
 * 現在は買い物リストの同時操作制御（悲観的排他ロック）を提供します。
 */
package com.example.stockapp.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import com.example.stockapp.entities.Family;

import jakarta.persistence.LockModeType;

@Repository
public interface FamilyRepository extends JpaRepository<Family, Long> {
    /**
     * 【排他ロック取得】指定した家族レコードを行ロック（FOR UPDATE）付きで取得します。
     *
     * 同一家族に対する買い物リストの追加・一括購入処理を直列化し、
     * 複数端末からの同時更新によるデータ不整合や競合を防ぎます。
     *
     * ※トランザクション内（@Transactional）で呼び出す必要があります。
     * ※ロックはトランザクション終了（コミット/ロールバック）時に解放されます。
     *
     * @param id 家族ID（現在はダミー家族ID=1L）
     * @return ロックを取得した家族エンティティ（存在しない場合は空の Optional）
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Family> findFirstById(Long id);
}
