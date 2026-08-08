CREATE TABLE IF NOT EXISTS public.cart_items
(
    "user_Id" bigint NOT NULL DEFAULT nextval('"cart_items_user_Id_seq"'::regclass),
    product_id bigint NOT NULL DEFAULT nextval('cart_items_product_id_seq'::regclass),
    quantity bigint NOT NULL,
    CONSTRAINT cart_items_pkey PRIMARY KEY ("user_Id", product_id),
    CONSTRAINT fk_product_id FOREIGN KEY (product_id)
    REFERENCES public.products (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION,
    CONSTRAINT fk_user_id FOREIGN KEY ("user_Id")
    REFERENCES public.users (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION
    )

    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.cart_items
    OWNER to marketplace_user;

CREATE TABLE IF NOT EXISTS public.categories
(
    id bigint NOT NULL DEFAULT nextval('categories_id_seq'::regclass),
    name character varying COLLATE pg_catalog."default",
    CONSTRAINT categories_pkey PRIMARY KEY (id),
    CONSTRAINT uq_categories UNIQUE (name)
    )

    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.categories
    OWNER to marketplace_user;

CREATE TABLE IF NOT EXISTS public.order_items
(
    id bigint NOT NULL DEFAULT nextval('order_items_id_seq'::regclass),
    order_id bigint NOT NULL DEFAULT nextval('order_items_order_id_seq'::regclass),
    product_id bigint NOT NULL DEFAULT nextval('order_items_product_id_seq'::regclass),
    quantity bigint NOT NULL,
    price_at_pruchase numeric(10,2) NOT NULL,
    status character varying COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT order_items_pkey PRIMARY KEY (id),
    CONSTRAINT fk_order_id FOREIGN KEY (order_id)
    REFERENCES public.orders (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION,
    CONSTRAINT fk_product_id FOREIGN KEY (product_id)
    REFERENCES public.products (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION,
    CONSTRAINT chk_order_items_status CHECK (status::text = ANY (ARRAY['ACTIVE'::character varying, 'CANCELLED'::character varying]::text[]))
    )

    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.order_items
    OWNER to marketplace_user;

CREATE TABLE IF NOT EXISTS public.orders
(
    id bigint NOT NULL DEFAULT nextval('orders_id_seq'::regclass),
    buyer_id bigint NOT NULL DEFAULT nextval('orders_buyer_id_seq'::regclass),
    status character varying COLLATE pg_catalog."default" NOT NULL,
    created_at timestamp with time zone NOT NULL,
                             CONSTRAINT orders_pkey PRIMARY KEY (id),
    CONSTRAINT fk_buyer_id FOREIGN KEY (buyer_id)
    REFERENCES public.users (id) MATCH SIMPLE
                         ON UPDATE NO ACTION
                         ON DELETE NO ACTION,
    CONSTRAINT chk_orders_status CHECK (status::text = ANY (ARRAY['NEW'::character varying, 'PROCESSING'::character varying, 'SHIPPED'::character varying, 'DELIVERED'::character varying, 'CANCELLED'::character varying]::text[])) NOT VALID
    )

    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.orders
    OWNER to marketplace_user;

CREATE TABLE IF NOT EXISTS public.product_images
(
    id bigint NOT NULL DEFAULT nextval('product_images_id_seq'::regclass),
    product_id bigint NOT NULL DEFAULT nextval('product_images_product_id_seq'::regclass),
    path character varying COLLATE pg_catalog."default" NOT NULL,
    "order" bigint NOT NULL,
    CONSTRAINT product_images_pkey PRIMARY KEY (id),
    CONSTRAINT fk_product_id FOREIGN KEY (product_id)
    REFERENCES public.products (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION
    )

    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.product_images
    OWNER to marketplace_user;

CREATE TABLE IF NOT EXISTS public.products
(
    id bigint NOT NULL DEFAULT nextval('products_id_seq'::regclass),
    seller_id bigint NOT NULL DEFAULT nextval('products_seller_id_seq'::regclass),
    category_id bigint NOT NULL DEFAULT nextval('products_category_id_seq'::regclass),
    name character varying COLLATE pg_catalog."default" NOT NULL,
    description character varying COLLATE pg_catalog."default" NOT NULL,
    price numeric(10,2) NOT NULL,
    "left" bigint NOT NULL,
    is_deleted boolean NOT NULL DEFAULT false,
    CONSTRAINT products_pkey PRIMARY KEY (id),
    CONSTRAINT fk_category_id FOREIGN KEY (category_id)
    REFERENCES public.categories (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION,
    CONSTRAINT fk_seller_id FOREIGN KEY (seller_id)
    REFERENCES public.users (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION
    )

    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.products
    OWNER to marketplace_user;

CREATE TABLE IF NOT EXISTS public.roles
(
    id bigint NOT NULL DEFAULT nextval('roles_id_seq'::regclass),
    name character varying COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT roles_pkey PRIMARY KEY (id),
    CONSTRAINT uq_roles_name UNIQUE (name)
    )

    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.roles
    OWNER to marketplace_user;

CREATE TABLE IF NOT EXISTS public.users
(
    id bigint NOT NULL DEFAULT nextval('users_id_seq'::regclass),
    role_id bigint NOT NULL DEFAULT nextval('users_role_id_seq'::regclass),
    login character varying COLLATE pg_catalog."default" NOT NULL,
    password_hash character varying COLLATE pg_catalog."default" NOT NULL,
    address character varying COLLATE pg_catalog."default",
    CONSTRAINT users_pkey PRIMARY KEY (id),
    CONSTRAINT uq_login UNIQUE (login),
    CONSTRAINT fk_role_id FOREIGN KEY (role_id)
    REFERENCES public.roles (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION
    )

    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.users
    OWNER to marketplace_user;

CREATE TABLE IF NOT EXISTS public.wishlist_items
(
    user_id bigint NOT NULL DEFAULT nextval('wishlist_items_user_id_seq'::regclass),
    product_id bigint NOT NULL DEFAULT nextval('wishlist_items_product_id_seq'::regclass),
    CONSTRAINT wishlist_items_pkey PRIMARY KEY (user_id, product_id),
    CONSTRAINT fk_product_id FOREIGN KEY (product_id)
    REFERENCES public.products (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION,
    CONSTRAINT fk_user_id FOREIGN KEY (user_id)
    REFERENCES public.users (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION
    )

    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.wishlist_items
    OWNER to marketplace_user;