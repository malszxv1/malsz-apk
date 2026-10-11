const express = require('express');
const router = express.Router();
const adminController = require('../controllers/adminController');
const { authenticate } = require('../middleware/auth');
const { requireRoles, checkUserManagementPermission } = require('../middleware/permissions');

router.use(authenticate);

// List users & stats: Accessible by OWNER, ADMIN, RESELLER (scoped)
router.get('/users', requireRoles('OWNER', 'ADMIN', 'RESELLER'), adminController.listUsers);
router.get('/stats', requireRoles('OWNER', 'ADMIN', 'RESELLER'), adminController.getStats);
router.get('/audit-logs', requireRoles('OWNER', 'ADMIN'), adminController.getAuditLogs);

// Create user: OWNER and RESELLER (and ADMIN)
router.post('/users', requireRoles('OWNER', 'ADMIN', 'RESELLER'), adminController.createUser);

// Specific user actions: checks permission boundary
router.get('/users/:id', requireRoles('OWNER', 'ADMIN', 'RESELLER'), checkUserManagementPermission, adminController.getUser);
router.post('/users/:id/ban', requireRoles('OWNER', 'ADMIN', 'RESELLER'), checkUserManagementPermission, adminController.banUser);
router.post('/users/:id/unban', requireRoles('OWNER', 'ADMIN', 'RESELLER'), checkUserManagementPermission, adminController.unbanUser);
router.patch('/users/:id', requireRoles('OWNER', 'ADMIN'), checkUserManagementPermission, adminController.updateUser);
router.delete('/users/:id', requireRoles('OWNER', 'ADMIN', 'RESELLER'), checkUserManagementPermission, adminController.deleteUser);

module.exports = router;
