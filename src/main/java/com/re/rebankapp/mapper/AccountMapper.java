package com.re.rebankapp.mapper;

import com.re.rebankapp.dto.response.AccountResponse;
import com.re.rebankapp.entity.Account;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    AccountResponse toAccountResponse(Account account);

    List<AccountResponse> toAccountResponseList(List<Account> accounts);
}
