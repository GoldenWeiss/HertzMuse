package test;

import static org.junit.Assert.*;

import org.junit.Test;

import Exception.ConstructorException;
import model.music.Note;

public class TestNote
{

	@Test
	public void testNoteDoubleInt()
	{
		// Invalid test

		try
		{
			new Note(-1, 440);
			fail("failed test negative time");
		} catch (ConstructorException e)
		{
		}
		try
		{
			new Note(1, -1);
			fail("failed test negative frequency");
		} catch (ConstructorException e)
		{
		}
		
		//Valid test
		
		try
		{
			new Note(0, 440);
		
		} catch (ConstructorException e)
		{
			fail("Failed test valid time 0");
		}
		
		try
		{
			new Note(1, 0);
		
		} catch (ConstructorException e)
		{
			fail("Failed test valid frequency 0");
		}
		
		
		
	}

	@Test
	public void testGetTime()
	{
		Note n1 = new Note(1,0);
		Note n2 = new Note(2,0);
		Note n3 = new Note(4,0);
		
		assertTrue(n1.getTime() == 1);
		assertTrue(n2.getTime() == 2);
		assertTrue(n3.getTime() == 8);
	}

	@Test
	public void testSetTime()
	{
		Note n1 = new Note(1,0);
		
		n1.setTime(0);
		assertTrue(n1.getTime() == 0);
		
		n1.setTime(2);
		assertTrue(n1.getTime() == 2);
		
		n1.setTime(-1);
		assertTrue(n1.getTime() == 2);
	}

	@Test
	public void testGetFrequency()
	{
		Note n1 = new Note(1,0);
		Note n2 = new Note(1,220);
		Note n3 = new Note(1,440);
		
		assertTrue(n1.getFrequency() == 0);
		assertTrue(n2.getFrequency() == 220);
		assertTrue(n3.getFrequency() == 440);
	}

	@Test
	public void testSetFrequency()
	{
		Note n1 = new Note(1,440);
		
		n1.setFrequency(0);
		assertTrue(n1.getFrequency() == 0);
		
		n1.setFrequency(220);
		assertTrue(n1.getFrequency() == 220);
		
		n1.setFrequency(-1);
		assertTrue(n1.getFrequency() == 220);
	}

	@Test
	public void testIsHeld()
	{
		Note n1 = new Note(1,1);
		Note n2 = new Note(1,1,false);
		Note n3 = new Note(1,1,true);
		
		assertTrue(!n1.isHeld());
		assertTrue(!n2.isHeld());
		assertTrue(n3.isHeld());
				
	}

}
