package example.local.statistics.wifi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.HashMap;

public class Utils{
	protected static final Logger logger = Utils.createLogger();

	public static Logger createLogger(){
		return LoggerFactory.getLogger(new Throwable().getStackTrace()[1].getClassName());
	}

	public static String logError(final Throwable ex){
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		return sw.toString();
	}

	public static String getHexStringFromByte(final byte b){
		String s = Integer.toHexString(b);
		s = (s.length() - 2) > 0 ? s.substring(s.length() - 2) : (s.length() == 1 ? "0" + s : s);
		return s.toUpperCase();
	}

	public static String getHexStringFromBytes(final byte[] array){
		StringBuilder str = new StringBuilder();
		for (final byte b : array){
			str.append("[").append(Utils.getHexStringFromByte(b)).append("]");
		}
		return str.toString();
	}

	public static String setStringLengthTo40(final String str){
		return setStringLengthTo40(str, ' ');
	}

	public static String setStringLengthTo40(final String str, final char symbol){
		int length = 40;
		return setStringLengthTo(str, symbol, length);
	}

	public static String setStringLengthTo(final String str, final char symbol, final int length){
		if (str.length() > length){
			return str.substring(0, length);
		}
		if (str.length() < length){
			StringBuilder stringBuilder = new StringBuilder(str);
			while (stringBuilder.length() < length){
				stringBuilder.append(symbol);
			}
			return stringBuilder.toString();
		}
		return str;
	}

	public static byte[] getBytesWin1251FromString(final String str){
		return str.getBytes(Charset.forName("windows-1251"));
	}

	public static <X, Y> HashMap<X, Y> mapFromByteArray(byte[] bytes){
		if (bytes != null){
			ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
			ObjectInput in = null;
			try{
				in = new ObjectInputStream(bis);
				//noinspection unchecked
				HashMap<X, Y> sold = (HashMap<X, Y>)in.readObject();
				if (sold == null){
					sold = new HashMap<>();
				}
				return sold;
			} catch (ClassNotFoundException | IOException e){
				logger.error(logError(e));
			} finally {
				try{
					bis.close();
				} catch (IOException e){
					logger.error(logError(e));
				}
				try{
					if (in != null){
						in.close();
					}
				} catch (IOException e){
					logger.error(logError(e));
				}
			}
		}
		return new HashMap<>();
	}

	public static <X, Y> byte[] byteArrayFromMap(HashMap<X, Y> map){
		if (map == null){
			map = new HashMap<>();
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		try{
			out = new ObjectOutputStream(bos);
			out.writeObject(map);
			return bos.toByteArray();
		} catch (IOException e){
			logger.error(logError(e));
		} finally {
			try{
				if (out != null){
					out.close();
				}
			} catch (IOException e){
				logger.error(logError(e));
			}
			try{
				bos.close();
			} catch (IOException e){
				logger.error(logError(e));
			}
		}
		return new byte[]{};
	}
}
