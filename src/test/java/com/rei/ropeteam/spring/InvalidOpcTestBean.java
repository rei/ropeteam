package com.rei.ropeteam.spring;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import com.rei.ropeteam.EventPublisher;

public class InvalidOpcTestBean {
    @OnePerCluster
    public int oncePerCluster() {
        return 0;
    }
}
