
package com.sussex.ase1.gpstry3;

import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class ExampleUnitTest {

    @Test
    public void addition_isCorrect() throws Exception {
        MainActivity myUnit = new MainActivity();

        assertTrue (myUnit.validPostcode("BN1 9PE"));
    }


    //@Test
    //public void test1()  {
        //  create mock
       // MainActivity test = Mockito.mock(MainActivity.class);

        // define return value for method getUniqueId()
        //when(test.testMock()).thenReturn(true);

        // use mock in test....
        //assertEquals(test.testMock(), true);
    //}
}