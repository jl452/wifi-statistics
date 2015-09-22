package example.local.statistics.wifi.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

@Entity
@Table(name = "item", uniqueConstraints = @UniqueConstraint(columnNames = {"time", "mac", "type", "signalLevel", "monitorMac"}))
public class Item implements Serializable{
	@Id
	@GeneratedValue(strategy = javax.persistence.GenerationType.SEQUENCE, generator = "item_SEQUENCE_GENERATOR")
	@SequenceGenerator(name = "item_SEQUENCE_GENERATOR", sequenceName = "item_sequence")
	private long id;
	@Temporal(TemporalType.TIMESTAMP)
	private Date time;
	private long monitorMac;
	private long mac;
	private int signalLevel;
	private String type;

	/*@Column(unique = true)
	private String hash;*/

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Collection<ItemFlag> flags;

	private Item(){
	}

	public Item(Date time, long monitorMac, long mac, int signalLevel, String type){
		this.time = time;
		this.monitorMac = monitorMac;
		this.mac = mac;
		this.signalLevel = signalLevel;
		this.type = type;
//		hash = new StringBuilder(Long.toHexString(time.getTime())).reverse().append(Long.toHexString(mac)).append(type).append(-signalLevel).append(Long.toHexString(monitorMac)).toString();
	}

	public Date getTime(){
		return time;
	}

	public long getMonitorMac(){
		return monitorMac;
	}

	public long getMac(){
		return mac;
	}

	public int getSignalLevel(){
		return signalLevel;
	}

	public String getType(){
		return type;
	}

	/*public String getHash(){
		return hash;
	}*/

	public Collection<ItemFlag> getFlags(){
		return flags;
	}
}
