package org.airavata.teamzenith.dao;

/**
 * This class is used to access data for the User entity.
 * Repository annotation allows the component scanning support to find and 
 * configure the DAO wihtout any XML configuration and also provide the Spring 
 * exceptiom translation.
 * Since we've setup setPackagesToScan and transaction manager on
 * DatabaseConfig, any bean method annotated with Transactional will cause
 * Spring to magically call begin() and commit() at the start/end of the
 * method. If exception occurs it will also call rollback().
 */
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
@Repository
@Transactional
public class UserJobDataDao {
  
  /**
   * Save the user in the database.
   */
  public void create(UserJobData job) {
    entityManager.merge(job);
    entityManager.flush();
    return;
  }
  
  /**
   * Delete the user from the database.
   */
  public void delete(UserJobData job) {
    if (entityManager.contains(job))
      entityManager.remove(job);
    else
      entityManager.remove(entityManager.merge(job));
    return;
  }
  
  /**
   * Return all the users stored in the database.
   */
  @SuppressWarnings("unchecked")
  public List getAll() {
    return entityManager.createQuery("from TZ_USER_JOB_INFO").getResultList();
  }
  
  /**
   * Return the user having the passed email.
   */
  public UserData getByEmail(long jNum) {
    return (UserData) entityManager.createQuery(
        "from TZ_USER_JOB_INFO where JobNumber= :jn")
        .setParameter("jn", jNum)
        .getSingleResult();
  }
  /**
   * Return the job Id based on the job number.
   */
  public UserJobData getByName(String jNum) {
	  return (UserJobData) entityManager.createQuery(
		        "from UserJobData where JobNumber= :jn")
		        .setParameter("jn", jNum)
		        .getSingleResult();  
	  }
  /**
   * Return the user having the passed id.
   */
  public UserJobData getById(long id) {
    return entityManager.find(UserJobData.class, id);
  }

  /**
   * Update the passed user in the database.
   */
  public void update(UserJobData job) {
    entityManager.merge(job);
    return;
  }

  // Private fields
  
  // An EntityManager will be automatically injected from entityManagerFactory
  // setup on DatabaseConfig class.
  @PersistenceContext
  private EntityManager entityManager;
  
}