package example.local.statistics.wifi.service;

import example.local.statistics.wifi.Utils;
import example.local.statistics.wifi.dao.WifiStatisticsDao;
import example.local.statistics.wifi.model.Item;
import example.local.statistics.wifi.model.Monitor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

@Service
//@Transactional(propagation =  Propagation.REQUIRED, readOnly = true)
//bug http://stackoverflow.com/questions/10181807/spring-3-1-hibernate-4-1-propagation-supports-issue
public class WifiStatisticsService{
	private static final Logger logger = Utils.createLogger();
	public static final int LOG_FORMAT_ELEMENTS_COUNT = 8;
	public static final int LOG_OLD_FORMAT_ELEMENTS_COUNT = 5;

	@Autowired
	protected WifiStatisticsDao dao;

	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	public List<Monitor> getAllMonitors(){
		return dao.getAll(Monitor.class);
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public long updateMonitor(long monitorMac, String description){
		Monitor item = dao.getById(Monitor.class, monitorMac);
		if (item != null){
			item.setDescription(description);
		} else {
			item = new Monitor(monitorMac, description);
		}
		dao.saveOrUpdate(item);
		return item.getId();
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void removeMonitor(long itemId){
		Monitor item = dao.getById(Monitor.class, itemId);
		if (item != null){
			dao.delete(item);
		} else {
			throw new IllegalArgumentException("Monitor not found with id=[" + itemId + "]");
		}
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	public List<String> getMonitorMacs(){
		List<Long> monitorMacs = dao.getMonitorMacs();
		List<String> monitorMacsString = new ArrayList<>();
		for (Long monitorMac : monitorMacs){
			monitorMacsString.add(Long.toHexString(monitorMac));
		}
		return monitorMacsString;
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	public List<Date> getDatesRange(long[] monitorMacs){
		return dao.getDatesRange(monitorMacs);
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	public long getUniqueMacsCount(long[] monitorMacs, Date startDate, Date endDate){
		return dao.getUniqueMacsCount(monitorMacs, startDate, endDate);
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void save(Serializable object){
		dao.save(object);
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public int addItemsInner(Iterator<Item> iterator){
		return dao.insertBatchObjectsByBatchSize(iterator);
	}

	//транзакция делается тока если вызывать функцию c Transactional снаружи,
	//если из этого класса вызвать то уже не сделается
	public int addItems(List<Item> items, int from){
		int iteratorPointer = from;
		long startTime = System.currentTimeMillis();
		int newCount = 0;

		boolean needRepeat = false;
		while (iteratorPointer < items.size() && (iteratorPointer - from) < WifiStatisticsDao.BATCH_SIZE * WifiStatisticsDao.BATCH_GROUP_SIZE){
			ListIterator<Item> iterator = items.listIterator(iteratorPointer);
			if (!needRepeat){
				try{
					int count = addItemsInner(iterator);
					iteratorPointer += count;
					newCount += count;
				} catch (Exception e){
//					logger.error("95456365436 " + String.valueOf(e.getClass()) + "_" + String.valueOf(e.getMessage()));
					//java.sql.SQLException: Violation of unique constraint UK_LJXDY1F7DDMNVP0J4HFVQP19: duplicate value(s) for column(s) HASH in statement [insert into public.item (hash, macId, monitorMacId, signalLevel, time, type, id) values (?, ?, ?, ?, ?, ?, ?)]
					//org.postgresql.util.PSQLException: ERROR: duplicate key value violates unique constraint "uk_ljxdy1f7ddmnvp0j4hfvqp19"|  Detail: Key (hash)=(81da95676415462743318459SA Probe Request899210269239380) already exists.
					boolean notKnowException = true;
					for (Throwable cause = e.getCause(); cause != null; cause = cause.getCause()){
//						logger.error("95456365436= " + String.valueOf(cause.getClass()) + "_" + String.valueOf(cause.getMessage()));
						if (cause instanceof SQLException){
							String message = cause.getMessage();
							if ((message.contains("Violation of unique constraint") && message.contains("duplicate value")) //hsqldb SQLException
									|| (message.contains("duplicate key value violates unique constraint"))){ //postgresql PSQLException
								needRepeat = true;
								notKnowException = false;
								break;
							} else {
								throw e;
							}
						}
					}
					if (notKnowException){
						throw e;
					}
				}
			} else {
				for (int i1 = 0; i1 < WifiStatisticsDao.BATCH_SIZE && iterator.hasNext(); i1++){
					Item item = iterator.next();
					try{
						save(item);
						newCount++;
					} catch (Exception e){
//						logger.error("25456365436 " + String.valueOf(e.getClass()) + "_" + String.valueOf(e.getMessage()));
						//java.sql.SQLException: Violation of unique constraint UK_LJXDY1F7DDMNVP0J4HFVQP19: duplicate value(s) for column(s) HASH in statement [insert into public.item (hash, macId, monitorMacId, signalLevel, time, type, id) values (?, ?, ?, ?, ?, ?, ?)]
						//org.postgresql.util.PSQLException: ERROR: duplicate key value violates unique constraint "uk_ljxdy1f7ddmnvp0j4hfvqp19"|  Detail: Key (hash)=(81da95676415462743318459SA Probe Request899210269239380) already exists.
						boolean notKnowException = true;
						for (Throwable cause = e.getCause(); cause != null; cause = cause.getCause()){
//							logger.error("25456365436= " + String.valueOf(cause.getClass()) + "_" + String.valueOf(cause.getMessage()));
							if (cause instanceof SQLException){
								String message = cause.getMessage();
								if ((message.contains("Violation of unique constraint") && message.contains("duplicate value")) //hsqldb SQLException
										|| (message.contains("duplicate key value violates unique constraint"))){ //postgresql PSQLException
									logger.debug("addItems duplicate: " + new Date(item.getTime().getTime()) + " monitorMac: " + Long.toHexString(item.getMonitorMac()) + " mac: " + Long.toHexString(item.getMac()) + " signal: " + item.getSignalLevel() + "dB type: " + item.getType() + "\n" + e);
									notKnowException = false;
									break;
								} else {
									throw e;
								}
							}
						}
						if (notKnowException){
							throw e;
						}
					}
					iteratorPointer++;
				}
				needRepeat = false;
			}
			if ((!needRepeat && iteratorPointer % 10000 == 0) || System.currentTimeMillis() - startTime > 10 * 1000){
				logger.debug("DAO addItems " + iteratorPointer + " new: " + newCount + " " + (iteratorPointer - from));
				startTime = System.currentTimeMillis();
			}
		}
		return newCount;
	}

	/**
	 * @param fromDir  for example "./../../"
	 * @param fileMask for example "log-filtered-wifi-.*\\.log"
	 * @return long[3] {eventsCount, newEventsCount, parseErrorsCount}
	 */
	public long[] load(String fromDir, final String fileMask){
		File[] files = new File(fromDir).listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name){
				return name.matches(fileMask);
			}
		});
		if (files == null){
			throw new IllegalArgumentException("Can't find files for import. fromDir=[" + fromDir + "] and fileMask=[" + fileMask + "]");
		}

		int eventsCount = 0, newEventsCount = 0, parseErrorsCount = 0;
		for (File file : files){
			logger.info("import log file: " + file.getAbsolutePath());
			try{
				BufferedReader in = new BufferedReader(new FileReader(file));
				long[] counts = load(in);
				eventsCount += counts[0];
				newEventsCount += counts[1];
				parseErrorsCount += counts[2];
			} catch (IOException e){
				logger.error(Utils.logError(e));
			}
		}
		return new long[]{eventsCount, newEventsCount, parseErrorsCount};
	}

	/**
	 * @param in BufferedReader
	 * @return long[3] {eventsCount, newEventsCount, parseErrorsCount}
	 */
	public long[] load(BufferedReader in){
		Map<Long, Map<Long, Map<String, Map<Date, List<Integer>>>>> items = new HashMap<>();
		int eventsCount = 0, newEventsCount = 0, parseErrorsCount = 0;
		String s;
		try{
			while ((s = in.readLine()) != null){
				try{
					String[] elements = s.split(",", LOG_FORMAT_ELEMENTS_COUNT);
					switch (elements.length){
						case LOG_OLD_FORMAT_ELEMENTS_COUNT:{
							Date date = new Date(Long.valueOf(elements[0]) * 1000);
							Long monitorMac = Long.parseLong(elements[1].replace(":", "").replace("Broadcast", "").replace("broadcast", ""), 16);
							int signalLevel = Integer.valueOf(elements[3]);
							String type = elements[4];
							Long sourceMac = Long.parseLong(elements[2].replace(":", "").replace("Broadcast", "").replace("broadcast", ""), 16);
							addLogItem(items, date, monitorMac, sourceMac, signalLevel, type);
							break;
						}
						case LOG_FORMAT_ELEMENTS_COUNT:{
//1401246006,08:60:6e:d3:88:54,00:1b:ba:d8:fc:81,98:fe:94:d7:0b:1f,-90,BSSID:00:1b:ba:d8:fc:81,Probe Response,Beeline_WiFi_Starbucks_FREE
							Date date = new Date(Long.valueOf(elements[0]) * 1000);
							Long monitorMac = Long.parseLong(elements[1].replace(":", "").replace("Broadcast", "").replace("broadcast", ""), 16);
							int signalLevel = Integer.valueOf(elements[4]);
							String bssid = elements[5];
							String type = elements[6];
							String wifiName = elements[7];
							try{
								Long sourceMac = Long.parseLong(elements[2].replace(":", "").replace("Broadcast", "").replace("broadcast", ""), 16);
								addLogItem(items, date, monitorMac, sourceMac, signalLevel, "SA," + type + "," + bssid + "," + wifiName);
							} catch (NumberFormatException ignored){
								logger.error("Input format error: [" + s + "]");
							}
							Long destinationMac = Long.parseLong(elements[3].replace(":", "").replace("Broadcast", "").replace("broadcast", ""), 16);
							addLogItem(items, date, monitorMac, destinationMac, 0, "DA," + type + "," + bssid + "," + wifiName);
							break;
						}
						default:
							logger.error("Input format error: [" + s + "]");
							parseErrorsCount++;
							continue;
					}
					eventsCount++;
				} catch (NumberFormatException ignored){
					logger.error("Input format error: [" + s + "]");
					parseErrorsCount++;
					continue;
				}
			}
			newEventsCount = addItems(items);
			logger.info("imported " + eventsCount + " strings, added new and unique: " + newEventsCount);
		} catch (IOException e){
			logger.error(Utils.logError(e));
		} catch (Exception e){
			logger.error(Utils.logError(e));
			throw e;
		}
		return new long[]{eventsCount, newEventsCount, parseErrorsCount};
	}

	protected static void addLogItem(Map<Long, Map<Long, Map<String, Map<Date, List<Integer>>>>> items, Date date, Long monitorMac, long mac, int signalLevel, String type){
		Map<Long, Map<String, Map<Date, List<Integer>>>> itemsByMonitorMac = items.get(monitorMac);
		if (itemsByMonitorMac == null){
			itemsByMonitorMac = new HashMap<>();
		}
		Map<String, Map<Date, List<Integer>>> itemsByMac = itemsByMonitorMac.get(mac);
		if (itemsByMac == null){
			itemsByMac = new HashMap<>();
		}
		Map<Date, List<Integer>> byDate = itemsByMac.get(type);
		if (byDate == null){
			byDate = new HashMap<>();
		}
		List<Integer> oldSignalLevel = byDate.get(date);
		if (oldSignalLevel == null){
			oldSignalLevel = new ArrayList<>();
		}
		oldSignalLevel.add(signalLevel);
		byDate.put(date, oldSignalLevel);
		itemsByMac.put(type, byDate);
		itemsByMonitorMac.put(mac, itemsByMac);
		items.put(monitorMac, itemsByMonitorMac);
	}

	public int addItems(Map<Long, Map<Long, Map<String, Map<Date, List<Integer>>>>> items/*, Map<String, Long> macIds*/){
		List<Item> itemsList = new ArrayList<>();
		int allCount = 0;
		long startTime = System.currentTimeMillis();
		for (Long monitorMac : items.keySet()){
			Map<Long, Map<String, Map<Date, List<Integer>>>> byMonitorMac = items.get(monitorMac);
			for (Long mac : byMonitorMac.keySet()){
				Map<String, Map<Date, List<Integer>>> byMac = byMonitorMac.get(mac);
				for (String type : byMac.keySet()){
					Map<Date, List<Integer>> byType = byMac.get(type);
					for (Date date : byType.keySet()){
						List<Integer> levels = byType.get(date);
						long i1 = 0;
						int count = 0;
						for (int signalLevel : levels){
							if (signalLevel != 0){
								i1 += signalLevel;
								count++;
							}
						}
						if (count > 0){
							i1 /= count;
						}

						Item item = new Item(date, monitorMac, mac, Long.valueOf(i1).intValue(), type);
						itemsList.add(item);
						allCount++;

						if (allCount % 10000 == 0 || System.currentTimeMillis() - startTime > 10 * 1000){
							logger.debug("addItems " + String.valueOf(allCount));
							startTime = System.currentTimeMillis();
						}
					}
				}
			}
		}
		int newCount = 0;
		for (int i1 = 0; i1 < itemsList.size(); i1 += WifiStatisticsDao.BATCH_SIZE * WifiStatisticsDao.BATCH_GROUP_SIZE){
			newCount += addItems(itemsList, i1);
		}
		return newCount;
	}


	//TODO REMOVE
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public long test1(){
		long count = dao.getCount(Item.class);
		logger.info(String.valueOf("Uniq items: " + count));
		return count;
	}
}