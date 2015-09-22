package example.local.statistics.wifi.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "monitor")
public class Monitor implements Serializable{
	@Id
//	@GeneratedValue(strategy = javax.persistence.GenerationType.SEQUENCE, generator = "monitor_SEQUENCE_GENERATOR")
//	@SequenceGenerator(name = "monitor_SEQUENCE_GENERATOR", sequenceName = "monitor_sequence")
	@Column(unique = true)
	private long id; //monitorMac
//	@Column(unique = true)
	private String description;

	private Monitor(){
	}

	public Monitor(long monitorMac, String description){
		this.id = monitorMac;
		this.description = description;
	}

	public long getId(){
		return id;
	}

	public void setId(long id){
		this.id = id;
	}

	public String getDescription(){
		return description;
	}

	public void setDescription(String description){
		this.description = description;
	}
}
