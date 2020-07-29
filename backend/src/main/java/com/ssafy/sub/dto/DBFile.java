package com.ssafy.sub.dto;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity
@Table(name = "files")
public class DBFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private int id;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private int fid;
	
    private String name;

    private String type;

    @Lob
    private byte[] data;

	public DBFile(int fid, String name, String type, byte[] data) {
		this.fid = fid;
		this.name = name;
		this.type = type;
		this.data = data;
	}
    
    

}