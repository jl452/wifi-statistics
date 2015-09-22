package example.local.statistics.wifi.dao;

import example.local.statistics.wifi.Utils;
import example.local.statistics.wifi.model.Item;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Repository
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class WifiStatisticsDao{
	private static final Logger logger = Utils.createLogger();
	public static final int BATCH_SIZE = 50;
	public static final int BATCH_GROUP_SIZE = 100;

	@Autowired
	protected SessionFactory sessionFactory;

	protected Session getSession(){
		return sessionFactory.getCurrentSession();
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void save(Serializable object){
		getSession().save(object);
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void saveOrUpdate(Serializable object){
		getSession().saveOrUpdate(object);
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void delete(Serializable object){
		getSession().delete(object);
	}

	public <X> X getById(Class<X> clazz, long id){
		return getSession().get(clazz, id);
	}

	public <X> List<X> getAll(Class<X> clazz){
		//noinspection unchecked
		return getSession().createCriteria(clazz).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
	}

	public <X> List<X> getAllBy(Class<X> clazz, String fieldName, Object fieldValue){
		//noinspection unchecked
		return getSession().createCriteria(clazz).add(Property.forName(fieldName).eq(fieldValue)).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
	}

	public <X> List<X> getAllBy(Class<X> clazz, String fieldName1, Object fieldValue1, String fieldName2, Object fieldValue2){
		//noinspection unchecked
		return getSession().createCriteria(clazz).add(Property.forName(fieldName1).eq(fieldValue1)).add(Property.forName(fieldName2).eq(fieldValue2)).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
	}

	public <X> List<X> getAllByIn(Class<X> clazz, String fieldName, Collection valueCollection){
		//noinspection unchecked
		return getSession().createCriteria(clazz).add(Property.forName(fieldName).in(valueCollection)).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
	}

	public <X> List<X> getAllByIn(Class<X> clazz, String fieldName, Object[] valueCollection){
		//noinspection unchecked
		return getSession().createCriteria(clazz).add(Property.forName(fieldName).in(valueCollection)).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
	}

	public <X> List<X> getAllByAndJoinBy(Class<X> clazz, String fieldName, Object fieldValue, String joinField){
		//noinspection unchecked
		return getSession().createCriteria(clazz).add(Property.forName(fieldName).eq(fieldValue)).setFetchMode(joinField, FetchMode.JOIN).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
	}

	public <X> X getFirstBy(Class<X> clazz, String fieldName, Object fieldValue){
		//noinspection unchecked
		return (X)getSession().createCriteria(clazz).add(Property.forName(fieldName).eq(fieldValue)).setMaxResults(1).uniqueResult();
	}

	public <X> X getFirstBy(Class<X> clazz, String fieldName1, Object fieldValue1, String fieldName2, Object fieldValue2){
		//noinspection unchecked
		return (X)getSession().createCriteria(clazz).add(Property.forName(fieldName1).eq(fieldValue1)).add(Property.forName(fieldName2).eq(fieldValue2)).setMaxResults(1).uniqueResult();
	}

	public long getCount(Class clazz){
		return (long)getSession().createCriteria(clazz).setProjection(Projections.projectionList().add(Projections.rowCount())).uniqueResult();
	}


	public List<Long> getMonitorMacs(){
		//noinspection unchecked
		return getSession().createCriteria(Item.class)
				.setProjection(Projections.projectionList()
								.add(Projections.groupProperty("monitorMac"))
				).list();
	}

	public List<Date> getDatesRange(long[] monitorMacs){
		Criteria criteria1 = getSession().createCriteria(Item.class)
				.setProjection(Projections.min("time"));
		Criterion disjunction = Restrictions.or(Restrictions.eq("monitorMac", monitorMacs[0]));
		for (int i1 = 1; i1 < monitorMacs.length; i1++){
			disjunction = Restrictions.or(Restrictions.eq("monitorMac", monitorMacs[i1]), disjunction);
		}
		criteria1.add(disjunction);
		Date minTime = (Date)criteria1.setMaxResults(1).uniqueResult();
		Criteria criteria2 = getSession().createCriteria(Item.class)
				.setProjection(Projections.max("time"));
		criteria2.add(disjunction);
		Date maxTime = (Date)criteria2.setMaxResults(1).uniqueResult();
		ArrayList<Date> list = new ArrayList<>();
		list.add(minTime);
		list.add(maxTime);
		return list;
	}

	public long getUniqueMacsCount(long[] monitorMacs, Date startDate, Date endDate){
		Criteria criteria = getSession().createCriteria(Item.class)
				.add(Restrictions.between("time", startDate, endDate))
				.setProjection(Projections.projectionList()
								.add(Projections.countDistinct("mac"))
				);
		Criterion disjunction = Restrictions.or(Restrictions.eq("monitorMac", monitorMacs[0]));
		for (int i1 = 1; i1 < monitorMacs.length; i1++){
			disjunction = Restrictions.or(Restrictions.eq("monitorMac", monitorMacs[i1]), disjunction);
		}
		criteria.add(disjunction);
		return (long)criteria.uniqueResult();
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void insertBatchObjects(Iterable<? extends Serializable> iterator){
		int count = 0;
		long startTime = System.currentTimeMillis();
		for (Serializable object : iterator){
			getSession().save(object);
			count++;

			if (count % BATCH_SIZE == 0){ //50, same as the JDBC batch size
				//flush a batch of inserts and release memory:
				getSession().flush();
				getSession().clear();
			}
			if (count % 10000 == 0 || System.currentTimeMillis() - startTime > 10 * 1000){
				logger.debug("DAO insertBatchObjects " + count);
				startTime = System.currentTimeMillis();
			}
		}
		getSession().flush();
		getSession().clear();
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public int insertBatchObjectsByBatchSize(Iterator<? extends Serializable> iterator){
		int count = 0;
		while (iterator.hasNext() && count < BATCH_SIZE){
			Serializable object = iterator.next();
			getSession().save(object);
			count++;
		}
		getSession().flush();
		getSession().clear();
		return count;
	}

	//TODO REMOVE
	/*public ItemState getItemStateCurrent(long refrigeratorId, boolean isPlanogram){
		return (ItemState)getSession().createCriteria(ItemState.class)
				.add(Restrictions.eq("refrigeratorId", refrigeratorId))
				.add(isPlanogram ? Restrictions.isNull("planogram") : Restrictions.isNotNull("planogram"))
				.addOrder(Order.desc("creationTime")).setMaxResults(1).uniqueResult();
	}

	public List<ItemState> getItemStateByDate(long refrigeratorId, boolean isPlanogram, Date startDate, Date endDate){
		//noinspection unchecked
		return getSession().createCriteria(ItemState.class)
				.add(Restrictions.eq("refrigeratorId", refrigeratorId))
				.add(isPlanogram ? Restrictions.isNull("planogram") : Restrictions.isNotNull("planogram"))
				.add(Restrictions.between("creationTime", startDate, endDate))
				.list();
	}

	public ItemState getItemStateFirstBefore(long refrigeratorId, boolean isPlanogram, Date endDate){
		//noinspection unchecked
		return (ItemState)getSession().createCriteria(ItemState.class)
				.add(Restrictions.eq("refrigeratorId", refrigeratorId))
				.add(isPlanogram ? Restrictions.isNull("planogram") : Restrictions.isNotNull("planogram"))
				.add(Restrictions.lt("creationTime", endDate))
				.addOrder(Order.desc("creationTime")).setMaxResults(1).uniqueResult();
	}

	public Shop getShopByItemId(long refrigeratorId){
		return (Shop)getSession().createCriteria(Shop.class, "shop").createAlias("shop.refrigerators", "refrigerator").add(Property.forName("refrigerator.id").eq(refrigeratorId)).uniqueResult();
	}


	public List<ItemShelf> listTest(){
		@SuppressWarnings("unchecked")
		List<ItemShelf> lines = (List<ItemShelf>)getSession().createCriteria(ItemShelf.class).setFetchMode("lines", FetchMode.JOIN).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		return lines;
	}

	@SuppressWarnings("unchecked")
	public List<ItemShelf> listTest2(long id){
//		return (List<ItemShelf>)getSession().createCriteria(ItemShelf.class).add(Property.forName("lines").eq(id).setFetchMode("lines", FetchMode.JOIN).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		return (List<ItemShelf>)getSession().createCriteria(ItemShelf.class, "shelf").createAlias("shelf.lines", "line").add(Property.forName("line.id").eq(id)).setFetchMode("lines", FetchMode.JOIN).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		//getSession().load(ItemShelf.class,Id)
	}*/
}
