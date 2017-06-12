package io.github.bedwarsrel.villager;

import java.util.Comparator;

public class MerchantCategoryComparator implements Comparator<MerchantCategory> {

  @Override
  public int compare(MerchantCategory o1, MerchantCategory o2) {

    int order1 = o1.getOrder();
    int order2 = o2.getOrder();

    return Integer.valueOf(order1).compareTo(Integer.valueOf(order2));
  }

}
