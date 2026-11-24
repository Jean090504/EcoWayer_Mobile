-- MySQL | EcoWayer | Simulação do extrato de Maxwillian Santana (usuário 16).
-- Use em ambiente de teste. Execute o arquivo inteiro na mesma conexão.
-- Por padrão, ROLLBACK desfaz a simulação depois da conferência.
-- Para gravar os registros de teste, troque o ROLLBACK final por COMMIT.
-- Depois de gravado, o extrato é imutável pelas regras do banco.

START TRANSACTION;

SET @usuario = NULL;
SELECT u.id INTO @usuario
FROM tbl_usuario u
WHERE u.id = 16
  AND EXISTS (
      SELECT 1 FROM tbl_email e
      WHERE e.id_usuario = u.id AND e.email = 'maxwilliansantana04@gmail.com'
  )
FOR UPDATE;

SET @lote = 'simulacao:extrato:usuario-16:v1';
-- Usa a collation de tbl_extrato_pontos.chave_idempotencia nas comparações.
SET @padrao_lote = CONVERT(CONCAT(@lote, ':%') USING utf8mb4) COLLATE utf8mb4_unicode_ci;
SET @agora = UTC_TIMESTAMP();
SET @papel = (SELECT MIN(id) FROM tbl_categoria WHERE nome = 'Papel');
SET @plastico = (SELECT MIN(id) FROM tbl_categoria WHERE nome = 'Plástico');
SET @vidro = (SELECT MIN(id) FROM tbl_categoria WHERE nome = 'Vidro');

SET @pode_inserir = (
    @usuario IS NOT NULL
    AND @papel IS NOT NULL AND @plastico IS NOT NULL AND @vidro IS NOT NULL
    AND NOT EXISTS (
        SELECT 1 FROM tbl_extrato_pontos
        WHERE id_usuario = @usuario AND chave_idempotencia LIKE @padrao_lote COLLATE utf8mb4_unicode_ci
    )
);

-- 20 dias atrás: +150 pontos por papel.
INSERT INTO tbl_pontos (qntd_pontos, data_registro, id_categoria, id_usuario)
SELECT 150, @agora - INTERVAL 20 DAY, @papel, @usuario WHERE @pode_inserir;
SET @papel_id = IF(ROW_COUNT() = 1, LAST_INSERT_ID(), NULL);

INSERT INTO tbl_extrato_pontos
    (id_usuario, id_pontos, tipo, quantidade, descricao, data_movimentacao, chave_idempotencia)
SELECT @usuario, @papel_id, 'credito', 150, '[SIMULAÇÃO] Reciclagem de papel',
       @agora - INTERVAL 20 DAY, CONCAT(@lote, ':papel')
WHERE @papel_id IS NOT NULL;

-- 14 dias atrás: +250 pontos por plástico.
INSERT INTO tbl_pontos (qntd_pontos, data_registro, id_categoria, id_usuario)
SELECT 250, @agora - INTERVAL 14 DAY, @plastico, @usuario WHERE @pode_inserir;
SET @plastico_id = IF(ROW_COUNT() = 1, LAST_INSERT_ID(), NULL);

INSERT INTO tbl_extrato_pontos
    (id_usuario, id_pontos, tipo, quantidade, descricao, data_movimentacao, chave_idempotencia)
SELECT @usuario, @plastico_id, 'credito', 250, '[SIMULAÇÃO] Reciclagem de plástico',
       @agora - INTERVAL 14 DAY, CONCAT(@lote, ':plastico')
WHERE @plastico_id IS NOT NULL;

-- 7 dias atrás: +500 pontos por vidro.
INSERT INTO tbl_pontos (qntd_pontos, data_registro, id_categoria, id_usuario)
SELECT 500, @agora - INTERVAL 7 DAY, @vidro, @usuario WHERE @pode_inserir;
SET @vidro_id = IF(ROW_COUNT() = 1, LAST_INSERT_ID(), NULL);

INSERT INTO tbl_extrato_pontos
    (id_usuario, id_pontos, tipo, quantidade, descricao, data_movimentacao, chave_idempotencia)
SELECT @usuario, @vidro_id, 'credito', 500, '[SIMULAÇÃO] Reciclagem de vidro',
       @agora - INTERVAL 7 DAY, CONCAT(@lote, ':vidro')
WHERE @vidro_id IS NOT NULL;

-- 3 dias atrás: −400 pontos em um resgate de teste, já marcado como utilizado.
INSERT INTO tbl_voucher (
    nome_parceiro, beneficio, codigo, data_resgate, data_expiracao,
    status, pontos_utilizados, id_usuario
)
SELECT 'Parceiro de teste', '[SIMULAÇÃO] Cupom de 15%', 'SIMULACAO-EXTRATO-U16-V1',
       @agora - INTERVAL 3 DAY, @agora + INTERVAL 27 DAY, 'utilizado', 400, @usuario
WHERE @pode_inserir;
SET @voucher_id = IF(ROW_COUNT() = 1, LAST_INSERT_ID(), NULL);

INSERT INTO tbl_extrato_pontos
    (id_usuario, id_voucher, tipo, quantidade, descricao, data_movimentacao, chave_idempotencia)
SELECT @usuario, @voucher_id, 'debito', 400, '[SIMULAÇÃO] Resgate de cupom',
       @agora - INTERVAL 3 DAY, CONCAT(@lote, ':resgate')
WHERE @voucher_id IS NOT NULL;

-- Conferência: 4 movimentos e impacto líquido de +500 pontos.
-- Os gatilhos do extrato atualizam tbl_saldo_pontos automaticamente.
SELECT @usuario AS usuario, @pode_inserir AS lote_inserido,
       COUNT(*) AS movimentos_do_lote,
       COALESCE(SUM(IF(tipo = 'debito', -quantidade, quantidade)), 0) AS total_liquido_lote,
       (SELECT saldo_atual FROM tbl_saldo_pontos WHERE id_usuario = @usuario) AS saldo_registrado
FROM tbl_extrato_pontos
WHERE id_usuario = @usuario AND chave_idempotencia LIKE @padrao_lote COLLATE utf8mb4_unicode_ci;

SELECT tipo, quantidade, descricao, data_movimentacao
FROM tbl_extrato_pontos
WHERE id_usuario = @usuario AND chave_idempotencia LIKE @padrao_lote COLLATE utf8mb4_unicode_ci
ORDER BY data_movimentacao DESC, id DESC;

ROLLBACK;
