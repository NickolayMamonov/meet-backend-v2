-- Add owner_id field to communities table
ALTER TABLE communities ADD COLUMN owner_id BIGINT;

-- For existing communities without owner, set owner to first user (if any)
UPDATE communities 
SET owner_id = (SELECT id FROM users ORDER BY id LIMIT 1)
WHERE owner_id IS NULL;

-- Make owner_id not null after setting values
ALTER TABLE communities ALTER COLUMN owner_id SET NOT NULL;

-- Add foreign key constraint
ALTER TABLE communities 
ADD CONSTRAINT fk_communities_owner 
FOREIGN KEY (owner_id) REFERENCES users(id);
