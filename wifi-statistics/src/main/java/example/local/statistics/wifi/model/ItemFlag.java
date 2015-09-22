package example.local.statistics.wifi.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "itemFlag")
public class ItemFlag implements Serializable{
	@Id
	@GeneratedValue(strategy = javax.persistence.GenerationType.SEQUENCE, generator = "itemFlag_SEQUENCE_GENERATOR")
	@SequenceGenerator(name = "itemFlag_SEQUENCE_GENERATOR", sequenceName = "itemFlag_sequence")
	private long id;
	@Column(unique = true)
	private String name;

	private ItemFlag(){
	}

	public ItemFlag(String name){
		this.name = name;
	}

	public long getId(){
		return id;
	}

	public String getName(){
		return name;
	}
}
