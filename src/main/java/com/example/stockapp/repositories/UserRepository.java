/*
 * 【役割】家族（グループ）およびユーザー情報に対するデータベース操作を担当します。
 * ※初期開発フェーズではログイン機能がないため、中身は空（もしくは後回し）でOKです。
 *
 * TODO: 後日、ログイン機能やユーザー登録機能を実装する際に追記する
 */
package com.example.stockapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.stockapp.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
