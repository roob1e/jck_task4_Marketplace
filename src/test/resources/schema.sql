create table public.roles
(
    id   bigserial
        primary key,
    name varchar not null
        constraint uq_roles_name
            unique
);

create table public.users
(
    id            bigserial
        primary key,
    role_id       bigint  not null
        constraint fk_role_id
            references public.roles
            on delete restrict,
    login         varchar not null
        constraint uq_login
            unique,
    password_hash varchar not null,
    address       varchar
);

create table public.categories
(
    id   bigserial
        primary key,
    name varchar
        constraint uq_categories
            unique
);

create table public.products
(
    id          bigserial
        primary key,
    seller_id   bigint                not null
        constraint fk_seller_id
            references public.users
            on delete restrict,
    category_id bigint                not null
        constraint fk_category_id
            references public.categories
            on delete restrict,
    name        varchar               not null,
    description varchar               not null,
    price       numeric(10, 2)        not null,
    "left"      bigint                not null,
    is_deleted  boolean default false not null,
    rating      numeric(2, 1)
);

create table public.product_images
(
    id         bigserial
        primary key,
    product_id bigint  not null
        constraint fk_product_id
            references public.products
            on delete cascade,
    path       varchar not null,
    "order"    bigint  not null
);

create table public.orders
(
    id         bigserial
        primary key,
    buyer_id   bigint                   not null
        constraint fk_buyer_id
            references public.users
            on delete restrict,
    status     varchar                  not null
        constraint chk_orders_status
            check ((status)::text = ANY
        ((ARRAY ['NEW'::character varying, 'PROCESSING'::character varying, 'SHIPPED'::character varying, 'DELIVERED'::character varying, 'CANCELLED'::character varying])::text[])),
    created_at timestamp with time zone not null
);

create table public.order_items
(
    id                bigserial
        primary key,
    order_id          bigint         not null
        constraint fk_order_id
            references public.orders
            on delete cascade,
    product_id        bigint         not null
        constraint fk_product_id
            references public.products
            on delete restrict,
    quantity          bigint         not null,
    price_at_purchase numeric(10, 2) not null,
    status            varchar        not null
        constraint chk_order_items_status
            check ((status)::text = ANY ((ARRAY ['ACTIVE'::character varying, 'CANCELLED'::character varying])::text[]))
    );

create table public.cart_items
(
    user_id    bigint not null
        constraint fk_user_id
            references public.users
            on delete cascade,
    product_id bigint not null
        constraint fk_product_id
            references public.products
            on delete cascade,
    quantity   bigint not null,
    primary key (user_id, product_id)
);

create table public.wishlist_items
(
    user_id    bigint not null
        constraint fk_user_id
            references public.users
            on delete cascade,
    product_id bigint not null
        constraint fk_product_id
            references public.products
            on delete cascade,
    primary key (user_id, product_id)
);

create table public.remember_tokens
(
    token_hash varchar                  not null
        primary key,
    user_id    bigint                   not null
        constraint fk_user_id
            references public.users
            on delete cascade,
    expires_at timestamp with time zone not null
);

create table public.reviews
(
    id            bigserial
        primary key,
    order_item_id bigint                   not null
        constraint fk_order_item_id
            references public.order_items,
    user_id       bigint                   not null
        constraint fk_user_id
            references public.users,
    product_id    bigint                   not null
        constraint fk_product_id
            references public.products,
    rating        smallint                 not null
        constraint chk_rating
            check ((rating >= 1) AND (rating <= 5)),
    created_at    timestamp with time zone not null,
    description   varchar,
    constraint uq_user_id_product_id
        unique (user_id, product_id)
);
