package com.gtan.spring.sub;

import com.gtan.spring.Component;

/**
 * @author gangtann@126.com
 * @version 1.0
 * @since 2025-06-29
 */
@Component
public class Cat {

    @Autowired
    private Dog dog;

    @PostConstruct
    public void init() {
        System.out.println("Cat init");
    }

}
