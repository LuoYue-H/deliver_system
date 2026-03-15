package org.example.deliver_system.service;

import org.example.deliver_system.entity.AddressBook;
import org.example.deliver_system.mapper.AddressBookMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressBookService {

    @Autowired
    private AddressBookMapper addressBookMapper;

    public List<AddressBook> findByUserId(Long userId) {
        return addressBookMapper.findByUserId(userId);
    }

    public void save(AddressBook addressBook) {
        addressBookMapper.insert(addressBook);
    }

    public void update(AddressBook addressBook) {
        addressBookMapper.update(addressBook);
    }

    public void deleteById(Long id) {
        addressBookMapper.deleteById(id);
    }

    @Transactional
    public void setDefault(Long id, Long userId) {
        addressBookMapper.clearDefaultByUserId(userId);
        AddressBook addressBook = addressBookMapper.findById(id);
        if (addressBook != null && addressBook.getUserId().equals(userId)) {
            addressBook.setIsDefault(1);
            addressBookMapper.update(addressBook);
        }
    }

    public AddressBook findById(Long id) {
        return addressBookMapper.findById(id);
    }

    @Transactional
    public void saveOrUpdateMerchantAddress(AddressBook addressBook) {
        List<AddressBook> existing = addressBookMapper.findByUserId(addressBook.getUserId());
        if (existing != null && !existing.isEmpty()) {
            AddressBook toUpdate = existing.get(0);
            toUpdate.setAddress(addressBook.getAddress());
            toUpdate.setContactName(addressBook.getContactName());
            toUpdate.setContactPhone(addressBook.getContactPhone());
            addressBookMapper.update(toUpdate);
        } else {
            addressBook.setIsDefault(0);
            addressBookMapper.insert(addressBook);
        }
    }
}
