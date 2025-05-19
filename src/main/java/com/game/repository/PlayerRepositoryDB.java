package com.game.repository;

import com.game.entity.Player;
import jakarta.annotation.PreDestroy;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository(value = "db")
public class PlayerRepositoryDB implements IPlayerRepository {
    private final SessionFactory sessionFactory;

    public PlayerRepositoryDB() {
        sessionFactory = new Configuration().addAnnotatedClass(Player.class).buildSessionFactory();
    }

    @Override
    public List<Player> getAll(int pageNumber, int pageSize) {
        NativeQuery<Player> nativeQuery = null;
        try (Session session = sessionFactory.openSession()){
            nativeQuery = session.createNativeQuery("SELECT * FROM rpg.player order by id asc", Player.class);
            nativeQuery.setFirstResult(pageNumber * pageSize);
            nativeQuery.setMaxResults(pageSize);
            return nativeQuery.list();
        }catch (HibernateException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getAllCount() {
        Query<Long> namedQuery;
        try (Session session = sessionFactory.openSession()){
            namedQuery = session.createNamedQuery("player_getAllCount", Long.class);
            return Integer.parseInt(String.valueOf(namedQuery.getSingleResult()));
        }catch (HibernateException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public Player save(Player player) {
        try (Session session = sessionFactory.openSession()){
            Transaction transaction = session.beginTransaction();
            session.persist(player);
            transaction.commit();
            return player;
        }
        catch (HibernateException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Player update(Player player) {
        try (Session session = sessionFactory.openSession()){
            Transaction transaction = session.beginTransaction();
            Player merge = session.merge(player);

            transaction.commit();
            return merge;
        }
        catch (HibernateException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Optional<Player> findById(long id) {
        Optional<Player> player = null;
        try (Session session = sessionFactory.openSession()){
            Query<Player> query = session.createQuery(String.format("from Player where id = %d", id), Player.class);
//            Player player1 = session.find(Player.class, id);
            return Optional.ofNullable(query.getSingleResult());
        }
        catch (HibernateException e){
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public void delete(Player player) {

        try (Session session = sessionFactory.openSession()){
            Transaction transaction = session.beginTransaction();
            session.remove(player);
            transaction.commit();
        }
        catch (HibernateException e){
            e.printStackTrace();
        }

    }

    @PreDestroy
    public void beforeStop() {
        sessionFactory.close();
    }
}