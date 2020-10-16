/* 
 * If not stated otherwise in this file or this component's Licenses.txt file the 
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 * Author: Stanislav Menshykov
 * Created: 1/14/16  3:27 PM
 */
package com.comcast.xconf.priority;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PriorityUtils {

    public static <T extends Prioritizable> List<T> addNewItemAndReorganize(T newItem, List<T> itemsList) {
        Collections.sort(itemsList, new PriorityComparator<T>());
        itemsList.add(itemsList.size(), newItem);
        final Integer oldPriority = itemsList.size();
        final Integer newPriority = newItem.getPriority();

        return reorganizePriorities(itemsList, oldPriority, newPriority);
    }

    public static <T extends Prioritizable> List<T> updateItemByPriorityAndReorganize(T itemToSave, List<T> itemsList, Integer currentItemPriority) {
        Collections.sort(itemsList, new PriorityComparator<T>());
        itemsList.set(currentItemPriority - 1, itemToSave);
        final Integer newPriority = itemToSave.getPriority();

        return reorganizePriorities(itemsList, currentItemPriority, newPriority);
    }

    public static <T extends Prioritizable> List<T> updatePriorities(List<T> itemsList, Integer oldPriority, Integer newPriority) {
        Collections.sort(itemsList, new PriorityComparator<T>());

        return reorganizePriorities(itemsList, oldPriority, newPriority);
    }

    /**
     * if we're had removed one item, we should update priorities.
     * For example:
     * Here is priorities after deleting item with itemPriority=4:
     * 1  2  3  5  6
     * Here is priorities after we made packPriorities() :
     * 1  2  3  4  5
     */
    public static <T extends Prioritizable> List<T> packPriorities(List<T> itemsList) {
        Collections.sort(itemsList, new PriorityComparator<T>());
        List<T> changedItems = new ArrayList<>();
        int priority = 1;
        for (T item : itemsList) {
            Integer oldPriority = item.getPriority();
            item.setPriority(priority);
            priority++;
            if (!item.getPriority().equals(oldPriority)) {
                changedItems.add(item);
            }
        }

        return changedItems;
    }

    /**
     * When we want to change itemPriority for one item
     * we should to displace other items.
     * If new itemPriority is higher than old itemPriority,
     * we are making displace down for all items
     * which have lower itemPriority than the new itemPriority.
     * If new itemPriority is lower than old itemPriority,
     * we are making displace up for all items
     * which have higher itemPriority than the new itemPriority.
     */
    private static <T extends Prioritizable> List<T> reorganizePriorities(List<T> sortedItemsList, Integer oldPriority, Integer newPriority) {
        if (newPriority == null || newPriority < 1 || newPriority > sortedItemsList.size()) {
            newPriority = sortedItemsList.size();
        }
        T item = sortedItemsList.get(oldPriority-1);
        item.setPriority(newPriority);

        if (oldPriority < newPriority) {
            for (int i = oldPriority; i<=newPriority-1; i++ ) {
                T buf = sortedItemsList.get(i);
                buf.setPriority(i);
                sortedItemsList.set(i-1, buf);
            }
        }

        if (oldPriority > newPriority) {
            for (int i = oldPriority-2; i>=newPriority-1; i-- ) {
                T buf = sortedItemsList.get(i);
                buf.setPriority(i+2);
                sortedItemsList.set(i + 1, buf);
            }
        }

        sortedItemsList.set(newPriority-1, item);

        return getAlteredSubList(sortedItemsList, oldPriority, newPriority);
    }

    private static <T extends Prioritizable> List<T> getAlteredSubList(List<T> itemsList, Integer oldPriority, Integer newPriority) {
        return itemsList.subList(Math.min(oldPriority, newPriority) - 1, Math.max(oldPriority, newPriority));
    }
}
