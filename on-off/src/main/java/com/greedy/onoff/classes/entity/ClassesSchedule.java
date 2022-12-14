package com.greedy.onoff.classes.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor 
@Getter	
@Setter
@Entity
@Table(name = "TBL_CLASSES_SCHEDULE")
@DynamicInsert
@SequenceGenerator(name = "CLASSES_SCHEDULE_SEQ_GENERATOR", 
sequenceName = "SEQ_CLASSES_SCHEDULE_CODE", 
initialValue = 1, allocationSize = 1)
public class ClassesSchedule {

	@Id
	@Column(name = "SCHEDULE_CODE")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CLASSES_SCHEDULE_SEQ_GENERATOR")
    private Long scheduleCode;

	@Column(name = "DAY_NAME")
    private String dayName;
	

	
	@Column(name = "CLASS_CODE")
    private Long classCode;


	@Column(name = "TIME_NAME")
    private String timeName;
	
	
}
