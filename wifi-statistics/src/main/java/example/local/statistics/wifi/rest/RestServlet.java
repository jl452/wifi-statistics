package example.local.statistics.wifi.rest;

import example.local.statistics.wifi.Utils;
import example.local.statistics.wifi.model.Monitor;
import example.local.statistics.wifi.service.WifiStatisticsService;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
//@Scope(value = "session")
@RequestMapping("/rest")
public class RestServlet /*implements ServletContextAware*/{
	private static final Logger logger = Utils.createLogger();

	@Autowired
	WifiStatisticsService service;

	/*@Override
	public void setServletContext(ServletContext servletContext){
		String username = servletContext.getInitParameter("username");
		String password = servletContext.getInitParameter("password");
	}*/

	/*<form action="http://localhost:8082/wifi-statistics/rest/load" method="post" enctype="multipart/form-data">
		<input name="importLogFile" type="file"/>
		<input type="submit"/>
	</form>*/

	/**
	 * @param importLogFile multipart/form-data from html "input" name="importLogFile" type="file"
	 * @return long[3] {eventsCount, newEventsCount, parseErrorsCount}
	 * @throws IOException
	 */
	@RequestMapping(value = "/load"/*, headers = "content-type=multipart*//*"*/)
	@ResponseBody
	public long[] load(@RequestParam MultipartFile importLogFile) throws IOException{
		logger.info("import log file: " + importLogFile.getOriginalFilename());
		return service.load(new BufferedReader(new InputStreamReader(importLogFile.getInputStream())));
	}

	/**
	 * @param fromDir  for example "./../../"
	 * @param fileMask for example "log-filtered-wifi-.*\\.log"
	 * @return long[3] {eventsCount, newEventsCount, parseErrorsCount}
	 * @throws IOException
	 */
	@RequestMapping(value = "/loadFromDir")
	@ResponseBody
	public long[] loadFromDir(@RequestParam String fromDir, @RequestParam String fileMask) throws IOException{
		return service.load(fromDir, fileMask);
	}

	@RequestMapping(value = "/getAllMonitors")
	@ResponseBody
	public List<Monitor> getAllMonitors(){
		return service.getAllMonitors();
	}

	@RequestMapping(value = "/updateMonitor")
	@ResponseBody
	public long updateMonitor(@RequestParam String monitorMac, @RequestParam String description){
		return service.updateMonitor(Long.parseLong(monitorMac.replace(":", ""), 16), description);
	}

	@RequestMapping(value = "/removeMonitor")
	@ResponseBody
	public void removeMonitor(@RequestParam String monitorMac){
		service.removeMonitor(Long.parseLong(monitorMac.replace(":", ""), 16));
	}

	@RequestMapping(value = "/getMonitors")
	@ResponseBody
	public List<String> getMonitors(){
		return service.getMonitorMacs();
	}

	@RequestMapping(value = "/getDatesRangeByMonitors")
	@ResponseBody
	public List<Date> getDatesRangeByMonitors(@RequestParam String[] monitorMacs) throws ParseException{
		testParams(monitorMacs);
		long[] monitorMacsLong = new long[monitorMacs.length];
		for (int i1 = 0; i1 < monitorMacs.length; i1++){
			monitorMacsLong[i1] = Long.parseLong(monitorMacs[i1].replace(":", ""), 16);
		}
		return service.getDatesRange(monitorMacsLong);
	}

	@RequestMapping(value = "/getUniqueMacsCountByMonitorsByDateString")
	@ResponseBody
	public long getUniqueMacsCountByMonitors(@RequestParam String[] monitorMacs, @RequestParam String startDate, @RequestParam String endDate) throws ParseException{
		testParams(monitorMacs);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd.hh:mm:ss");
		long[] monitorMacsLong = new long[monitorMacs.length];
		for (int i1 = 0; i1 < monitorMacs.length; i1++){
			monitorMacsLong[i1] = Long.parseLong(monitorMacs[i1].replace(":", ""), 16);
		}
		return service.getUniqueMacsCount(monitorMacsLong, df.parse(startDate), df.parse(endDate));
	}

	@RequestMapping(value = "/getUniqueMacsCountByMonitorsByDateLong")
	@ResponseBody
	public long getUniqueMacsCountByMonitors(@RequestParam String[] monitorMacs, @RequestParam long startDate, @RequestParam long endDate) throws ParseException{
		testParams(monitorMacs);
		long[] monitorMacsLong = new long[monitorMacs.length];
		for (int i1 = 0; i1 < monitorMacs.length; i1++){
			monitorMacsLong[i1] = Long.parseLong(monitorMacs[i1].replace(":", ""), 16);
		}
		return service.getUniqueMacsCount(monitorMacsLong, new Date(startDate), new Date(endDate));
	}

	private void testParams(String[] monitorMacs){
		if (monitorMacs.length < 1){
			throw new IllegalArgumentException("monitorMacs array length must be > 0. array length=[" + monitorMacs.length + "]");
		}
	}


	//TODO REMOVE
	@RequestMapping(value = "/test1")
	@ResponseBody
	public String test1() throws ParseException{
//		return service.test1();

		StringBuilder output = new StringBuilder();
		String tempOutput;
		List<String> monitors = getMonitors();
		for (String monitor : monitors){
			String[] monitorsMacs = {monitor};
			List<Date> datesRangeByMonitor = getDatesRangeByMonitors(monitorsMacs);
			DateTime startDate = new DateTime(datesRangeByMonitor.get(0).getTime());
			DateTime finishDate = new DateTime(datesRangeByMonitor.get(1).getTime());
			tempOutput = "Monitor: [" + monitor + "] startDate: [" + startDate + "] finishDate: [" + finishDate + "]";
			output.append(tempOutput);
			System.out.println(tempOutput);
			startDate = startDate.withField(DateTimeFieldType.secondOfDay(), 0);
//			startDate = new DateTime(2014, 8, 1, 0, 0);
			while (startDate.isBefore(finishDate)){
				DateTime nextDate = startDate.plusDays(1);
				long uniqueMacsCountByMonitor = getUniqueMacsCountByMonitors(monitorsMacs, startDate.getMillis(), nextDate.getMillis());
				tempOutput = "dayOfYear: " + startDate/*.dayOfYear()*/ + "\tuniqueMacsCount: " + uniqueMacsCountByMonitor;
				output.append(tempOutput);
				System.out.println(tempOutput);
				startDate = nextDate;
			}
		}
		return output.toString();
	}

	@RequestMapping(value = "/test2")
	@ResponseBody
	public long[] test2() throws ParseException{
		return new long[]{new Date().getTime(), service.test1()};
	}
}