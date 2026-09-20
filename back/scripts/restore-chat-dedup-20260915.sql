-- chat_messages 双写冗余数据清理 —— 回滚备份
-- 生成时间：2026-09-15
-- 说明：执行清理前把待删行原样导出。误删时直接跑本文件即可还原。
-- 背景：ChatWebSocketHandler 收到 type='message' 时入库，前端 deliverMessage 修复前
--       WS 入库之外又打了一次 /chat/send，导致同一条消息写两遍（现前端已修复）。
INSERT INTO chat_messages (id, room_id, sender_id, sender_type, content, msg_type, attach_url, created_at, updated_at) VALUES (120, '53368397_10241065', '10241065', 1, '11', 'text', '', '2026-09-12 12:41:01', '2026-09-12 12:41:01');
INSERT INTO chat_message_read (id, msg_id, room_id, user_id, is_read, created_at, read_time) VALUES (69, 120, '53368397_10241065', '53368397', 1, '2026-09-12 12:41:01', '2026-09-12 15:34:32');

