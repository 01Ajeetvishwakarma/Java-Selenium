package com.orangehrm.tests;

import com.orangehrm.base.BaseClass;
import org.testng.annotations.Test;

public class DummyTest2 extends BaseClass {

    @Test
    public void Dummy() {
        String title = driver.getTitle();
        assert title.equals("OrangeHRM"):"Failed";
        System.out.println("Title: "+title);
        pause(2);
    }

}
