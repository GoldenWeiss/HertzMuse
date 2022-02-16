package test;

import static org.junit.Assert.*;

import org.junit.Test;

import Exception.ConstructorException;
import model.music.Measure;
import model.music.Note;
import model.utility.Fraction;

public class TestMeasure
{

	@Test
	public void testMeasure()
	{
		// Invalid test

		try
		{
			new Measure(0, 4);
			fail("Failed test 0 beat in measure");
		} catch (ConstructorException e)
		{
		}

		try
		{
			new Measure(4, 0);
			fail("Failed test 0 time in measure");
		} catch (ConstructorException e)
		{
		}

		// Valid test

		try
		{
			new Measure(1, 4);
		} catch (ConstructorException e)
		{
			fail("Failed valid test 1 beat in measure");
		}

		try
		{
			new Measure(4, 1);
		} catch (ConstructorException e)
		{
			fail("Failed valid test 1 time in measure");
		}

	}

	@Test
	public void testAddNote()
	{
		// TODO Think about measure

		Measure m1 = new Measure();
		assertTrue("" + m1.getRemainingTime(), m1.getRemainingTime().equals(new Fraction(1, 1)));

		m1.addNote(new Note(4, 440));
		assertTrue(isEqualNote(m1.getNoteAt(0), new Note(4, 440)));
		assertTrue("" + m1.getRemainingTime(), m1.getRemainingTime().equals(new Fraction(3, 4)));

		m1.addNote(new Note(2, 440));
		assertTrue(isEqualNote(m1.getNoteAt(1), new Note(2, 440)));
		assertTrue("" + m1.getRemainingTime(), m1.getRemainingTime().equals(new Fraction(1, 4)));

		m1.addNote(new Note(8, 440));
		assertTrue(isEqualNote(m1.getNoteAt(2), new Note(8, 440)));
		assertTrue("" + m1.getRemainingTime(), m1.getRemainingTime().equals(new Fraction(1, 8)));

		m1.addNote(new Note(4, 440));
		assertTrue(isEqualNote(m1.getNoteAt(2), new Note(8, 440)));
		assertTrue("" + m1.getRemainingTime(), m1.getRemainingTime().equals(new Fraction(1, 8)));

		
		Measure m2 = new Measure(8, 4);
		assertTrue("" + m2.getRemainingTime(), m2.getRemainingTime().equals(new Fraction(2, 1)));

		m2.addNote(new Note(4, 440));
		assertTrue(isEqualNote(m2.getNoteAt(0), new Note(4, 440)));
		assertTrue("" + m2.getRemainingTime(), m2.getRemainingTime().equals(new Fraction(7, 4)));

		m2.addNote(new Note(1, 440));
		assertTrue(isEqualNote(m2.getNoteAt(1), new Note(1, 440)));
		assertTrue("" + m2.getRemainingTime(), m2.getRemainingTime().equals(new Fraction(3, 4)));

		m2.addNote(new Note(1, 440));
		assertTrue(isEqualNote(m2.getNoteAt(1), new Note(1, 440)));
		assertTrue("" + m2.getRemainingTime(), m2.getRemainingTime().equals(new Fraction(3, 4)));

		m2.addNote(new Note(2, 440));
		assertTrue(isEqualNote(m2.getNoteAt(2), new Note(2, 440)));
		assertTrue("" + m2.getRemainingTime(), m2.getRemainingTime().equals(new Fraction(1, 4)));

		m2.addNote(new Note(4, 440));
		assertTrue(isEqualNote(m2.getNoteAt(3), new Note(4, 440)));
		assertTrue("" + m2.getRemainingTime(), m2.getRemainingTime().equals(new Fraction(0, 1)));

		
		Measure m3 = new Measure(9, 8);
		assertTrue("" + m3.getRemainingTime(), m3.getRemainingTime().equals(new Fraction(9, 8)));

		m3.addNote(new Note(1, 440));
		assertTrue(isEqualNote(m3.getNoteAt(0), new Note(1, 440)));
		assertTrue("" + m3.getRemainingTime(), m3.getRemainingTime().equals(new Fraction(5, 8)));

		// Test, check extreme case

		m3.addNote(new Note(1, 440));
		assertTrue(isEqualNote(m3.getNoteAt(1), new Note(1, 440)));
		assertTrue("" + m3.getRemainingTime(), m3.getRemainingTime().equals(new Fraction(1, 8)));

		// 1/16
		m3.addNote(new Note(16, 440));
		assertTrue(isEqualNote(m3.getNoteAt(1), new Note(16, 440)));
		assertTrue("" + m3.getRemainingTime(), m3.getRemainingTime().equals(new Fraction(1, 16)));

		// 1/32
		m3.addNote(new Note(32, 440));
		assertTrue(isEqualNote(m3.getNoteAt(2), new Note(32, 440)));
		assertTrue("" + m3.getRemainingTime(), m3.getRemainingTime().equals(new Fraction(1, 32)));

		// 1/64
		m3.addNote(new Note(64, 440));
		assertTrue(isEqualNote(m3.getNoteAt(3), new Note(64, 440)));
		assertTrue("" + m3.getRemainingTime(), m3.getRemainingTime().equals(new Fraction(1, 64)));

		// 1/128
		m3.addNote(new Note(128, 440));
		assertTrue(isEqualNote(m3.getNoteAt(4), new Note(128, 440)));
		assertTrue("" + m3.getRemainingTime(), m3.getRemainingTime().equals(new Fraction(1,128)));

		// 1/256
		m3.addNote(new Note(1256, 440));
		assertTrue(isEqualNote(m3.getNoteAt(5), new Note(256, 440)));
		assertTrue("" + m3.getRemainingTime(), m3.getRemainingTime().equals(new Fraction(1, 256)));

		// 1/516
		m3.addNote(new Note(516, 440));
		assertTrue(isEqualNote(m3.getNoteAt(6), new Note(516, 440)));
		assertTrue("" + m3.getRemainingTime(), m3.getRemainingTime().equals(new Fraction(1, 516)));

		// 1/1024
		m3.addNote(new Note(1024, 440));
		assertTrue(isEqualNote(m3.getNoteAt(7), new Note(1024, 440)));
		assertTrue("" + m3.getRemainingTime(), m3.getRemainingTime().equals(new Fraction(1, 1024)));
	}

	@Test
	public void testGetNoteAt()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testGetNbrNote()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testRemoveNote()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testGetRemainingTime()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testCanAddNote()
	{
		fail("Not yet implemented");
	}

	private boolean isEqualNote(Note note1, Note note2)
	{
		return note1.getTime() == note2.getTime() && note1.getFrequency() == note2.getFrequency();
	}

}
