package ewing.faster.dao.impl;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import ewing.faster.dao.RoleDao;
import ewing.faster.dao.entity.Authority;
import ewing.faster.dao.entity.Role;
import ewing.faster.security.vo.FindRoleParam;
import ewing.faster.security.vo.RoleWithAuthority;
import ewing.query.BaseQueryFactory;
import ewing.query.QueryUtils;
import ewing.query.clause.BaseQuery;
import ewing.query.paging.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色数据访问实现。
 */
@Repository
public class RoleDaoImpl implements RoleDao {

    @Autowired
    private BaseQueryFactory queryFactory;

    private QBean<RoleWithAuthority> qRoleWithAuthority = Projections
            .bean(RoleWithAuthority.class, qRole.all());

    @Override
    public Page<RoleWithAuthority> findRoleWithAuthority(FindRoleParam findRoleParam) {
        // 查询角色总数
        BaseQuery<Role> roleQuery = queryFactory.selectFrom(qRole)
                .whereIfHasText(findRoleParam.getSearch(), qRole.name::contains);
        long total = roleQuery.fetchCount();

        // 分页查询并附带权限
        roleQuery.limit(findRoleParam.getLimit()).offset(findRoleParam.getOffset());
        List<Tuple> rows = queryFactory.select(qRoleWithAuthority, qAuthority)
                .from(roleQuery.as(qRole))
                .leftJoin(qRoleAuthority).on(qRole.roleId.eq(qRoleAuthority.roleId))
                .leftJoin(qAuthority).on(qRoleAuthority.authorityId.eq(qAuthority.authorityId))
                .fetch();

        return new Page<>(total, QueryUtils.rowsToTree(
                rows, qRoleWithAuthority, qAuthority,
                RoleWithAuthority::getRoleId,
                Authority::getAuthorityId,
                RoleWithAuthority::getAuthorities,
                RoleWithAuthority::setAuthorities));
    }

}
