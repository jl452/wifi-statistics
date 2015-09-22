package example.local.statistics.wifi.service;

import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.MutableDateTime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//@Service
@Deprecated
public class LoaderService{
	/*@Autowired
	protected WifiStatisticsService service;*/

	public static void main(final String[] args) throws IOException{
		if (args.length != 2){
			System.out.println("Syntax is:");
			System.out.println("java -jar wifi-statistics.jar example.local.statistics.wifi.service.LoaderService /DIR/ log-filtered-wifi-*MASK_REGEXP*.log");
			return;
		}

		load(args[0], args[1]);
	}

	public static void load(String fromDir, final String fileMask) throws IOException{
		File[] files = new File(fromDir).listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name){
//				return name.matches("log-filtered-wifi-.*\\.log");
				return name.matches(fileMask);
			}
		});

		for (File file : files){
			List<HashMap<String, List<MutableDateTime>>> dates = new ArrayList<>();
			HashMap<String, List<MutableDateTime>> items = new HashMap<>();

			int minimalSignal = Integer.MAX_VALUE;
			int allCount = 0;
			HashMap<String, List<MutableDateTime>> uniqItems = new HashMap<>();
			int oneSeeCount = 0;
			int oneTimeCount = 0;
			int repeatTimeCount = 0;
			int workerCount = 0;
			HashMap<Integer, HashSet<String>> byHour = new HashMap<>();

			MutableDateTime currentDate = null;
			MutableDateTime lastDate = null;
			System.out.println("Log file: " + file.getAbsolutePath());
			BufferedReader in = new BufferedReader(new FileReader(file));
			String s;
			while ((s = in.readLine()) != null){
				allCount++;
//		    	System.out.print(s + " >>> ");
				int elementsCounts = 5;
				String[] s1 = s.split(",", elementsCounts);
				if (s1.length != elementsCounts){
					System.out.println("Input format error: [" + s + "]");
					continue;
				}
				MutableDateTime date = new MutableDateTime(Long.valueOf(s1[0]) * 1000);
				String monitorMac = s1[1].replace(":", "");
				String mac = s1[2].replace(":", "");
				int signalLevel = Integer.valueOf(s1[3]);
				String type = s1[4];

				/*service.addItem(date.toDate(), monitorMac, mac, signalLevel, type);
				if (allCount % 5000 == 0){
					System.out.println(allCount);
				}*/

//		    	System.out.println(date.toString() + "_" + signalLevel + "_" + mac);

				if (signalLevel < minimalSignal){
					minimalSignal = signalLevel;
				}
				MutableDateTime day = (MutableDateTime)date.clone();
				day.setTime(0, 0, 0, 0);
				if (currentDate == null){
					currentDate = day;
					System.out.print("Started from " + date.toString());
				} else if (Days.daysBetween(currentDate, day).getDays() > 0){
					currentDate.addDays(1);
					System.out.print(" ended " + lastDate.toString() + " Uniq MAC count = " + items.size());

					Set<Integer> t1 = byHour.keySet();
					Integer[] t2 = t1.toArray(new Integer[t1.size()]);
					Arrays.sort(t2);
					for (Integer hour : t2){
						System.out.print(" " + hour + ":" + byHour.get(hour).size());
					}
					System.out.println();

					currentDate = day;
					dates.add(items);
					items = new HashMap<>();
					byHour = new HashMap<>();
					System.out.print("Started from " + date.toString());
				}
				List<MutableDateTime> list = items.get(mac);
				if (list == null){
					list = new ArrayList<>();
				}
				list.add(date);
				items.put(mac, list);

				list = uniqItems.get(mac);
				if (list == null){
					list = new ArrayList<>();
				}
				list.add(date);
				uniqItems.put(mac, list);

				int hour = date.getHourOfDay();
				HashSet<String> hourCount = byHour.get(hour);
				if (hourCount == null){
					hourCount = new HashSet<>();
					byHour.put(hour, hourCount);
				}
				hourCount.add(mac);

				lastDate = date;
			}
			dates.add(items);
			System.out.print(" ended " + String.valueOf(lastDate) + " Uniq MAC count = " + items.size());

			Set<Integer> t1 = byHour.keySet();
			Integer[] t2 = t1.toArray(new Integer[t1.size()]);
			Arrays.sort(t2);
			for (Integer hour : t2){
				System.out.print(" " + hour + ":" + byHour.get(hour).size());
			}
			System.out.println();

//	    	System.out.println("Hours between(>=2):");
			for (String i1 : uniqItems.keySet()){
				List<MutableDateTime> i2 = uniqItems.get(i1);
				boolean oneTimeFlag = true;
				boolean repeatTimeFlag = false;
				boolean workerFlag = false;
				if (i2.size() > 1){
					Iterator<MutableDateTime> i3 = i2.iterator();
					MutableDateTime i4 = i3.next();
					for (MutableDateTime i5 = i3.next(); ; i5 = i3.next()){
						int hours = Hours.hoursBetween(i4, i5).getHours();
						if (hours >= 2){
//					    	System.out.print(" " + hours);
							oneTimeFlag = false;
						}
						if (hours >= 48){
							repeatTimeFlag = true;
						}
						if (hours >= 9 && hours < 48){
							workerFlag = true;
						}
						i4 = i5;
						if (!i3.hasNext()){
							break;
						}
					}
				} else {
					oneSeeCount++;
				}
				if (oneTimeFlag){
					oneTimeCount++;
				}
				if (repeatTimeFlag){
					repeatTimeCount++;
				}
				if (workerFlag){
					workerCount++;
				}
			}

//			System.out.println();
			System.out.println("Minimal dB= " + minimalSignal + "dB");
			System.out.println("All count in log= " + allCount);
			System.out.println("OneSee MAC count= " + oneSeeCount);
			System.out.println("OneTime count= " + oneTimeCount);
			System.out.println("RepeatTime count= " + repeatTimeCount);
			System.out.println("Worker count= " + workerCount);
			System.out.println("Uniq MAC count= " + uniqItems.size());
			System.out.println();
		}
	}
}
