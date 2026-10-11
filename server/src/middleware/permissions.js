const userRepository = require('../repositories/userRepository');

function requireRoles(...allowedRoles) {
  return (req, res, next) => {
    if (!req.user) {
      return res.status(401).json({ error: 'Unauthenticated' });
    }
    if (!allowedRoles.includes(req.user.role)) {
      return res.status(403).json({
        error: 'Forbidden: Insufficient role permissions',
        requiredRoles: allowedRoles,
        userRole: req.user.role
      });
    }
    next;
    next();
  };
}

function checkUserManagementPermission(req, res, next) {
  const actor = req.user;
  const targetId = req.params.id;

  if (!targetId) return next();

  const target = userRepository.findById(targetId);
  if (!target) {
    return res.status(404).json({ error: 'Target user not found' });
  }

  req.targetUser = target;

  // Rule 1: OWNER has full management power, except deleting or banning another OWNER
  if (actor.role === 'OWNER') {
    if (target.role === 'OWNER' && target.id !== actor.id && (req.method === 'DELETE' || req.path.includes('/ban'))) {
      return res.status(403).json({ error: 'Cannot ban or delete another OWNER' });
    }
    return next();
  }

  // Rule 2: ADMIN can manage RESELLER and MEMBER only
  if (actor.role === 'ADMIN') {
    if (target.role === 'OWNER' || target.role === 'ADMIN') {
      return res.status(403).json({ error: 'Admin cannot modify or ban other Admins or Owners' });
    }
    return next();
  }

  // Rule 3: RESELLER can ONLY manage MEMBER created by them
  if (actor.role === 'RESELLER') {
    if (target.role !== 'MEMBER') {
      return res.status(403).json({ error: 'Resellers can only manage MEMBER accounts' });
    }
    if (target.created_by !== actor.id) {
      return res.status(403).json({ error: 'Reseller can only manage accounts they created' });
    }
    return next();
  }

  return res.status(403).json({ error: 'Members cannot manage other accounts' });
}

module.exports = {
  requireRoles,
  checkUserManagementPermission
};
